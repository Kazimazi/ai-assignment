package game.gmk.players;

import game.gmk.GomokuAction;
import game.gmk.GomokuGame;
import game.gmk.GomokuPlayer;
import game.gmk.ui.GameGraphicsController;
import java.util.Random;

public class HumanPlayer extends GomokuPlayer {
   public HumanPlayer(int color, int[][] board, Random r) {
      super(color, board, r);
   }

   public GomokuAction getAction(GomokuAction prevAction, long[] remainingTimes) {
      if (prevAction != null) {
         this.board[prevAction.i][prevAction.j] = 1 - this.color;
      }

      try {
         GomokuAction action = null;

         do {
            action = GameGraphicsController.getAction();
         } while(!GomokuGame.isValid(this.board, action));

         this.board[action.i][action.j] = this.color;
         return action;
      } catch (InterruptedException var4) {
         var4.printStackTrace();
         return null;
      }
   }
}
