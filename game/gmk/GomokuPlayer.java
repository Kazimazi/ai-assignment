package game.gmk;

import game.engine.Player;
import game.engine.utils.Pair;
import java.util.List;
import java.util.Random;

public abstract class GomokuPlayer implements Player<GomokuAction> {
   public final int color;
   public final int[][] board;
   public final Random random;

   public GomokuPlayer(int color, int[][] board, Random random) {
      this.color = color;
      this.board = board;
      this.random = random;
   }

   public final GomokuAction getAction(List<Pair<Integer, GomokuAction>> prevActions, long[] remainingTimes) {
      return this.getAction(prevActions.isEmpty() ? null : (GomokuAction)((Pair)prevActions.get(0)).second, remainingTimes);
   }

   public abstract GomokuAction getAction(GomokuAction var1, long[] var2);

   public final int getColor() {
      return this.color;
   }

   public final String toString() {
      return this.getClass().getName() + " " + this.color;
   }
}
