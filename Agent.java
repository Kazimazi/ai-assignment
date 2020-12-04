import java.util.ArrayList;
import java.util.Random;

import game.gmk.GomokuAction;
import game.gmk.GomokuGame;
import game.gmk.GomokuPlayer;

public class Agent extends GomokuPlayer {
    /** Array for all kinda good actions might not be tho. */
    protected ArrayList<GomokuAction> actions = new ArrayList<GomokuAction>(); // make some kind of set for it
    private boolean LOG = false;

    /**
     * Creates muh player, sets the specified parameters to the super class.
     * @param color player's color
     * @param board game board
     * @param random random number generator
     * @see GomokuPlayer#GomokuPlayer(int, int[][], Random)
     */
    public Agent(int color, int[][] board, Random random) {
        super(color, board, random);
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                actions.add(new GomokuAction(i, j));
            }
        }
    }

    @Override
    public GomokuAction getAction(GomokuAction prevAction, long[] remainingTimes) {
        if (prevAction == null) {
            // TODO you can do it better yeah?
            int i = board.length / 2;
            int j = board[i].length / 2;
            while (board[i][j] != GomokuGame.EMPTY) {
                i = random.nextInt(board.length);
                j = random.nextInt(board[i].length);
            }
            board[i][j] = color;
            return new GomokuAction(i, j);
        }

        board[prevAction.i][prevAction.j] = 1 - color;

        GomokuAction action = null; // init best move
        int score = Integer.MIN_VALUE; // -Inf
        for (GomokuAction a : actions) { // NOTE decrease number of actions
            if (board[a.i][a.j] == GomokuGame.EMPTY) {
                int s = negamax(board, 3, color); // TODO change depth according to remainingTimes, or state of game
                if (score < s) {
                    score = s;
                    action = a;
                }
            }
        }

        board[action.i][action.j] = 1;

        return action;
    }

    // TODO implement
    protected int scorePosition(int[][] board, int color) {
        return 0;
    }

    protected int[][] boardWithMove(int[][] board, GomokuAction action, int color) {
        board[action.i][action.j] = color;
        return board;
    }

    protected ArrayList<int[][]> getChildrenBoards(int[][] board, int color) {
        ArrayList<int[][]> boards = new ArrayList<>();
        for (GomokuAction action : actions) { // NOTE decrease number of actions
            boards.add(boardWithMove(board, action, color));
        }
        return boards;
    }

    protected int negamax(int[][] board, int depth, int player) {
        if (depth == 0 || GomokuGame.hasFive(board, color))
            return color * scorePosition(board, color);
        int value = Integer.MIN_VALUE;
        for (int[][] child : getChildrenBoards(board, color)) {
            value = Integer.max(value, -negamax(child, depth - 1, -color));
        }
        return value;
    }

    private void Log(String string) {
        if (LOG)
            System.out.println("~~~" + string);
    }
}

