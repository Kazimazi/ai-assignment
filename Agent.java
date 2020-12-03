import java.util.ArrayList;
import java.util.Random;

import game.gmk.GomokuAction;
import game.gmk.GomokuGame;
import game.gmk.GomokuPlayer;

public class Agent extends GomokuPlayer {
    /** Array for all possible actions. */
    protected ArrayList<GomokuAction> actions = new ArrayList<GomokuAction>();

    /**
     * Creates muh player, sets the specified parameters to the super class.
     * @param color player's color
     * @param board game board
     * @param random random number generator
     * @see GomokuPlayer#GomokuPlayer(int, int[][], Random)
     */
    public Agent(int color, int[][] board, Random random) {
        super(color, board, random);
        // init actions
        for (int i = 0; i < board.length; i++)
            for (int j = 0; j < board[i].length; j++)
                // only store legit moves
                if (board[i][j] == GomokuGame.EMPTY)
                    actions.add(new GomokuAction(i, j));
    }

    @Override
    public GomokuAction getAction(GomokuAction prevAction, long[] remainingTimes) {
        if (prevAction == null) return randomFirstMove();
        board[prevAction.i][prevAction.j] = 1 - color;
        return new GomokuAction(1, 1);
    }

    /**
     * Temporaly first move function.
     * @return first move
     */
    protected GomokuAction randomFirstMove() {
        int i = board.length / 2;
        int j = board[i].length / 2;
        while (board[i][j] != GomokuGame.EMPTY) {
            i = random.nextInt(board.length);
            j = random.nextInt(board[i].length);
        }
        board[i][j] = color;
        return new GomokuAction(i, j);
    }
}
