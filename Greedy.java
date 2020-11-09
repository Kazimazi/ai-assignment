import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import game.gmk.GomokuAction;
import game.gmk.GomokuGame;
import game.gmk.GomokuPlayer;

/**
 * An implementation of the {@link GomokuPlayer} that selects the cell of the
 * board for an action that has the most consecutive neighbors. If there is more
 * than one, chooses a random one.
 */
public class Greedy extends GomokuPlayer {
    /** Array for all possible actions. */
    protected ArrayList<GomokuAction> actions = new ArrayList<GomokuAction>();

    /**
     * Creates a greedy player, sets the specified parameters to the super class.
     * @param color player's color
     * @param board game board
     * @param random random number generator
     * @see GomokuPlayer#GomokuPlayer(int, int[][], Random)
     */
    public Greedy(int color, int[][] board, Random random) {
        super(color, board, random);
        // store possible actions
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                actions.add(new GomokuAction(i, j));
            }
        }
    }

    @Override
    public GomokuAction getAction(GomokuAction prevAction, long[] remainingTimes) {
        // I am the first, center start (not necessary)
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

        // store enemy's step
        board[prevAction.i][prevAction.j] = 1 - color;

        // find best steps and choose a random one
        Collections.shuffle(actions, random);
        GomokuAction action = null;
        int score = -1;
        for (GomokuAction a : actions) {
            if (board[a.i][a.j] == GomokuGame.EMPTY) {
                int s = score(a.i, a.j, color) + score(a.i, a.j, 1 - color);
                if (score < s) {
                    score = s;
                    action = a;
                }
            }
        }

        // store and do best step
        board[action.i][action.j] = color;
        System.out.println("!!!!!" + score);
        return action;
    }

    /**
     * The score of a cell is the aggregated value of the any direction from
     * there computed by {@link GreedyPlayer#countDirection(int, int, int, int, int)} function.
     * @param i row of the cell
     * @param j column of the cell
     * @param c color
     * @return aggregated neighbor score
     */
    protected int score(int i, int j, int c) {
        int result = 0;
        // right up
        result += 1 << countDirection(i, j, 1, -1, c);
        // right
        result += 1 << countDirection(i, j, 1, 0, c);
        // right down
        result += 1 << countDirection(i, j, 1, 1, c);
        // down
        result += 1 << countDirection(i, j, 0, 1, c);
        // left down
        result += 1 << countDirection(i, j, -1, 1, c);
        // left
        result += 1 << countDirection(i, j, -1, 0, c);
        // left up
        result += 1 << countDirection(i, j, -1, -1, c);
        // up
        result += 1 << countDirection(i, j, 0, -1, c);
        return result;
    }

    /**
     * Counts the number of consecutive cells from the specified start point at
     * the specified direction belongs to the specified color.
     * @param i row of start position
     * @param j column of start position
     * @param di row direction
     * @param dj column direction
     * @param c color
     * @return number of consecutive cells
     */
    protected int countDirection(int i, int j, int di, int dj, int c) {
        int ni = (i + board.length + di) % board.length;
        int nj = (j + board[ni].length + dj) % board[ni].length;
        if (board[ni][nj] != c) {
            return 0;
        }
        return 1 + countDirection(ni, nj, di, dj, c);
    }

}
