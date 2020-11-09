package game.gmk.players;

import game.gmk.GomokuAction;
import game.gmk.GomokuPlayer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

public class GreedyPlayer extends GomokuPlayer {
   protected ArrayList<GomokuAction> actions = new ArrayList();

   public GreedyPlayer(int color, int[][] board, Random random) {
      super(color, board, random);

      for(int i = 0; i < board.length; ++i) {
         for(int j = 0; j < board[i].length; ++j) {
            this.actions.add(new GomokuAction(i, j));
         }
      }

   }

   public GomokuAction getAction(GomokuAction prevAction, long[] remainingTimes) {
      int score;
      if (prevAction != null) {
         this.board[prevAction.i][prevAction.j] = 1 - this.color;
         Collections.shuffle(this.actions, this.random);
         GomokuAction action = null;
         score = -1;
         Iterator var5 = this.actions.iterator();

         while(var5.hasNext()) {
            GomokuAction a = (GomokuAction)var5.next();
            if (this.board[a.i][a.j] == 2) {
               int s = this.score(a.i, a.j, this.color) + this.score(a.i, a.j, 1 - this.color);
               if (score < s) {
                  score = s;
                  action = a;
               }
            }
         }

         this.board[action.i][action.j] = this.color;
         return action;
      } else {
         int i = this.board.length / 2;

         for(score = this.board[i].length / 2; this.board[i][score] != 2; score = this.random.nextInt(this.board[i].length)) {
            i = this.random.nextInt(this.board.length);
         }

         this.board[i][score] = this.color;
         return new GomokuAction(i, score);
      }
   }

   protected int score(int i, int j, int c) {
      int result = 0;
      int result = result + (1 << this.countDirection(i, j, 1, -1, c));
      result += 1 << this.countDirection(i, j, 1, 0, c);
      result += 1 << this.countDirection(i, j, 1, 1, c);
      result += 1 << this.countDirection(i, j, 0, 1, c);
      result += 1 << this.countDirection(i, j, -1, 1, c);
      result += 1 << this.countDirection(i, j, -1, 0, c);
      result += 1 << this.countDirection(i, j, -1, -1, c);
      result += 1 << this.countDirection(i, j, 0, -1, c);
      return result;
   }

   protected int countDirection(int i, int j, int di, int dj, int c) {
      int ni = (i + this.board.length + di) % this.board.length;
      int nj = (j + this.board[ni].length + dj) % this.board[ni].length;
      return this.board[ni][nj] != c ? 0 : 1 + this.countDirection(ni, nj, di, dj, c);
   }
}
