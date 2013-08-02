package com.alexshabanov.cameldemo.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Application components configuration.
 */
@Configuration
@ComponentScan(basePackages = "com.alexshabanov.cameldemo")
public class ComponentConfig {
}
