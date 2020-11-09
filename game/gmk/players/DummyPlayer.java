package game.gmk.players;

import game.gmk.GomokuAction;
import game.gmk.GomokuPlayer;
import java.util.Random;

public class DummyPlayer extends GomokuPlayer {
   public DummyPlayer(int color, int[][] board, Random random) {
      super(color, board, random);
   }

   public GomokuAction getAction(GomokuAction prevAction, long[] remainingTimes) {
      return null;
   }
}
