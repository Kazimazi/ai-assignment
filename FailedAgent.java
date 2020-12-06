import java.util.ArrayList;
import java.util.Random;

import game.gmk.GomokuAction;
import game.gmk.GomokuGame;
import game.gmk.GomokuPlayer;

public class FailedAgent extends GomokuPlayer {
    /** Array for all kinda good actions might not be tho. */
    protected ArrayList<GomokuAction> actions = new ArrayList<GomokuAction>(); // make some kind of set for it
    int times = 0;

    /**
     * Creates muh player, sets the specified parameters to the super class.
     * @param color player's color
     * @param board game board
     * @param random random number generator
     * @see GomokuPlayer#GomokuPlayer(int, int[][], Random)
     */
    public FailedAgent(int color, int[][] board, Random random) {
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

        GomokuAction action = null;
        int score = Integer.MIN_VALUE; // -Inf

        //ArrayList<GomokuAction> availableActions = getLimitedActions(board, 1);
        //for (GomokuAction a : availableActions) {
        for (GomokuAction a : actions) {
            if (board[a.i][a.j] == GomokuGame.EMPTY) {
                int s = negamaxAB(board, 1, Integer.MIN_VALUE, Integer.MAX_VALUE, color); // TODO change depth according to remainingTimes, or state of game
                //int s = scoreAction(board, a, color) + scoreAction(board, a, 1 - color);
                if (score < s) {
                    score = s;
                    action = a;
                }
            }
        }
        board[action.i][action.j] = color;

        return action;
    }

    protected int scoreAction(int[][] b, GomokuAction a, int c) {
        int result = 0;
        // right up
        result += 1 << countDirection(b, a, 1, -1, c);
        // right
        result += 1 << countDirection(b, a, 1, 0, c);
        // right down
        result += 1 << countDirection(b, a, 1, 1, c);
        // down
        result += 1 << countDirection(b, a, 0, 1, c);
        // left down
        result += 1 << countDirection(b, a, -1, 1, c);
        // left
        result += 1 << countDirection(b, a, -1, 0, c);
        // left up
        result += 1 << countDirection(b, a, -1, -1, c);
        // up
        result += 1 << countDirection(b, a, 0, -1, c);
        return result;
    }

    protected int countDirection(int[][] board, GomokuAction a, int di, int dj, int c) {
        int ni = (a.i + board.length + di) % board.length;
        int nj = (a.j + board[ni].length + dj) % board[ni].length;
        // bit of improvement compared to Greedy
        if (board[ni][nj] != c || board[ni][nj] == GomokuGame.WALL) {
            return 0;
        } else if (board[ni][nj] == GomokuGame.EMPTY) {
            return 1;
        }
        return 1 + countDirection(board, new GomokuAction(ni, nj), di, dj, c);
    }

    // TODO extend
    protected int staticEval(int[][] board, int color) {
        int score = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == GomokuGame.EMPTY) {
                    score = Integer.max(
                            score,
                            scoreAction(board, new GomokuAction(i, j), color) + scoreAction(board, new GomokuAction(i, j), 1 - color)
                            );
                }
            }
        }
        return score;
    }

    protected int[][] boardWithMove(int[][] board, GomokuAction action, int color) {
        board[action.i][action.j] = color;
        return board;
    }

    protected boolean isOccupiedByPlayer(int[][] board, GomokuAction a) {
        return board[a.i][a.j] == GomokuGame.COLOR_O || board[a.i][a.j] == GomokuGame.COLOR_X;
    }

    // TODO rewrite
    protected ArrayList<GomokuAction> getLimitedActions(int[][] board, int distance) {
        ArrayList<GomokuAction> results = new ArrayList<>();
        for (GomokuAction a : actions) {
            for (int ni = a.i - distance; ni != a.i + distance + 1; ni++) {
                int realI = (ni + board.length) % board.length;
                for (int nj = a.j - distance; nj != a.j + distance + 1; nj++) {
                    int realJ = (nj + board[realI].length) % board[realI].length;
                    //boolean isSameRowColumn = (realI == a.i && realJ == a.j);
                    if (board[a.i][a.j] == GomokuGame.EMPTY) {
                        if (isOccupiedByPlayer(board, new GomokuAction(realI, realJ))) {
                            results.add(a);
                        }
                    }
                }
            }
        }
        return results;
    }

    protected ArrayList<int[][]> getChildrenBoards(int[][] board, int color, int distance) {
        ArrayList<int[][]> boards = new ArrayList<>();
        ArrayList<GomokuAction> actions = getLimitedActions(board, distance);
        for (GomokuAction action : actions) {
            if (board[action.i][action.j] == GomokuGame.EMPTY) {
                boards.add(boardWithMove(GomokuGame.copy(board), action, color));
            }
        }
        return boards;
    }

    protected int negamaxAB(int[][] board, int depth, int alpha, int beta, int player) {
        if (depth == 0 || GomokuGame.hasFive(board, color))
            return color * staticEval(board, color);
        int value = Integer.MIN_VALUE;
        // NOTE could order childBoards
        for (int[][] child : getChildrenBoards(board, color, 2)) {
            value = Integer.max(value, -negamaxAB(child, depth - 1, -beta, -alpha, -color));
            alpha = Integer.max(alpha, value);
            if (alpha >= beta) break; // cut
        }
        return value;
    }
}
