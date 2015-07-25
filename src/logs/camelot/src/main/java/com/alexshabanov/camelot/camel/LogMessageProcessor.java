package com.alexshabanov.camelot.camel;

import com.alexshabanov.camelot.common.Constants;
import com.alexshabanov.camelot.model.LogMessage;
import com.alexshabanov.camelot.model.MaterializedLogMessage;
import com.alexshabanov.camelot.model.NullLogMessage;
import com.alexshabanov.camelot.model.Severity;
import com.alexshabanov.camelot.util.CommaSeparatedValueParser;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Message processor that converts a line into a message.
 *
 * @author Alexander Shabanov
 */
public final class LogMessageProcessor implements Processor {

  public static final Pattern RECORD_PATTERN = Pattern.compile(
      "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3}) " + // date+time
          "(\\p{Upper}+) " + // severity
          "([\\w\\.\\$]+) " + // class name
          "((?:[\\w]+=[\\w\\+/\\.\\$]+)(?:, (?:[\\w]+=[\\w\\+/\\.\\$]+))*)? " + // variables
          "\\[[\\w\\.\\$\\(\\)]+\\] " + // thread ID
          "(.+)" + // message
          "$"
  );

  private static final String METRIC_MARKER = Constants.METRIC + ' ';

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final DateFormat dateFormat;

  public LogMessageProcessor() {
    this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  @Override
  public void process(Exchange exchange) throws Exception {
    final String line = exchange.getIn().getBody(String.class);
    final LogMessage logMessage = parse(line);
    exchange.getOut().setBody(logMessage);
  }

  // visible for testing
  public LogMessage parse(String line) {
    final Matcher matcher = RECORD_PATTERN.matcher(line);
    if (!matcher.matches()) {
      return NullLogMessage.INSTANCE;
    }

    if (matcher.groupCount() < 5) {
      log.error("Count of groups is not six: actual={} for line={}", matcher.groupCount(), line);
      return NullLogMessage.INSTANCE;
    }

    final Date date;
    try {
      date = dateFormat.parse(matcher.group(1));
    } catch (ParseException e) {
      return NullLogMessage.INSTANCE;
    }

    final Severity severity = Severity.fromString(matcher.group(2), Severity.WARN);

    final MaterializedLogMessage logMessage = new MaterializedLogMessage(date.getTime(), severity, line);
    addAttributesFromVariables(logMessage, matcher.group(4));

    final String message = matcher.group(5);
    if (message != null) {
      final int metricIndex = message.indexOf(METRIC_MARKER);
      if (metricIndex >= 0) {
        addAttributesFromMetrics(logMessage, message.substring(metricIndex + METRIC_MARKER.length()));
      }
    }

    return logMessage;
  }

  //
  // Private
  //

  private void addAttributesFromMetrics(MaterializedLogMessage logMessage, String metricBody) {
    final CommaSeparatedValueParser parser = new CommaSeparatedValueParser(metricBody);
    final Map<String, String> vars = parser.readAsMap();
    logMessage.putAllAttributes(vars);
  }

  private void addAttributesFromVariables(MaterializedLogMessage logMessage, String variables) {
    if (variables == null) {
      return; // no variables
    }

    final CommaSeparatedValueParser parser = new CommaSeparatedValueParser(variables);
    final Map<String, String> vars = parser.readAsMap();
    logMessage.putAllAttributes(vars);
  }
}
