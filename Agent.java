import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import game.gmk.GomokuAction;
import game.gmk.GomokuGame;
import game.gmk.GomokuPlayer;

public class Agent extends GomokuPlayer {
    protected ArrayList<GomokuAction> actions = new ArrayList<GomokuAction>();

    public Agent(int color, int[][] board, Random random) {
        super(color, board, random);
    }

    @Override
    public GomokuAction getAction(GomokuAction prevAction, long[] remainingTimes) {
    }

    protected int score(int i, int j, int c) {
    }
}