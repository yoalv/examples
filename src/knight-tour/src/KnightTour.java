public final class KnightTour {
    private KnightTour() {}

    private static final int DEFAULT_BOARD_SIDE_SIZE = 8;

    private static final int DEFAULT_START_ROW = 0;
    private static final int DEFAULT_START_COLUMN = 0;

    private static final class Move {
        final int rowDelta;
        final int columnDelta;

        public Move(int rowDelta, int columnDelta) {
            this.rowDelta = rowDelta;
            this.columnDelta = columnDelta;
        }
    }

    private static final Move[] KNIGHT_MOVES = {
            new Move(2, 1), new Move(2, -1), new Move(1, 2), new Move(1, -2),
            new Move(-2, 1), new Move(-2, -1), new Move(-1, 2), new Move(-1, -2)
    };

    private static final class SolutionFinder {
        final int rows;
        final int columns;
        final int[][] board;
        final int maxMoves;

        public SolutionFinder(int columns, int rows) {
            this.columns = columns;
            this.rows = rows;
            this.board = new int[rows][columns];
            this.maxMoves = columns * rows;
        }

        boolean isLegalMove(int row, int column) {
            return row >= 0 && column >= 0 && row < rows && column < columns && board[row][column] == 0;
        }

        boolean move(int currentRow, int currentColumn, int currentMoveIndex) {
            board[currentRow][currentColumn] = currentMoveIndex;
            if (currentMoveIndex == maxMoves) {
                // solution found
                return true;
            }

            for (final Move move : KNIGHT_MOVES) {
                final int nextRow = currentRow + move.rowDelta;
                final int nextColumn = currentColumn + move.columnDelta;
                if (isLegalMove(nextRow, nextColumn) && move(nextRow, nextColumn, currentMoveIndex + 1)) {
                    return true;
                }
            }

            // this move is not a valid one, reset current mark
            board[currentRow][currentColumn] = 0;
            return false;
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("board:\n");
            for (int row = 0; row < rows; ++row) {
                for (int column = 0; column < columns; ++column) {
                    builder.append(String.format(" %2d", board[row][column]));
                }
                builder.append("\n");
            }
            return builder.toString();
        }
    }

    private static void findSolution(int rows, int columns, int startRow, int startColumn) {
        final SolutionFinder finder = new SolutionFinder(rows, columns);
        long timeSpent = System.nanoTime();
        final boolean found = finder.move(startRow, startColumn, 1);

        timeSpent = System.nanoTime() - timeSpent;
        System.out.println("Solution " + (found ? "found: " + finder : "not found") + "\nTime: " + timeSpent);
    }

    /**
     * Application's entry point.
     * @param args Provided arguments.
     */
    public static void main(String[] args) {
        // infer: width and height of the board
        final int rows = getArgParam(args, 0, DEFAULT_BOARD_SIDE_SIZE);
        final int columns = getArgParam(args, 1, DEFAULT_BOARD_SIDE_SIZE);

        System.out.println("KnightTour {rows} {columns}\nUsing width = " + rows + " and columns = " + columns);

        findSolution(rows, columns, DEFAULT_START_ROW, DEFAULT_START_COLUMN);
    }

    private static int getArgParam(String[] args, int index, int defaultValue) {
        if (index < args.length) {
            return Integer.parseInt(args[index]);
        }

        return defaultValue;
    }
}

