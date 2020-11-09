package game.gmk.players;

import game.gmk.GomokuAction;
import game.gmk.GomokuGame;
import game.gmk.GomokuPlayer;
import java.util.Random;

public class RandomPlayer extends GomokuPlayer {
   public RandomPlayer(int color, int[][] board, Random random) {
      super(color, board, random);
   }

   public GomokuAction getAction(GomokuAction prevAction, long[] remainingTimes) {
      if (prevAction != null) {
         this.board[prevAction.i][prevAction.j] = 1 - this.color;
      }

      GomokuAction action = null;
      double max = -1.0D;

      for(int i = 0; i < this.board.length; ++i) {
         for(int j = 0; j < this.board[i].length; ++j) {
            double value = GomokuGame.isValid(this.board, i, j) ? this.random.nextDouble() : -1.0D;
            if (max < value) {
               action = new GomokuAction(i, j);
               max = value;
            }
         }
      }

      this.board[action.i][action.j] = this.color;
      return action;
   }
}
