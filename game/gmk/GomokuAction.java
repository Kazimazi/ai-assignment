package game.gmk;

import game.engine.Action;

public final class GomokuAction implements Action {
   private static final long serialVersionUID = -7013856728856297441L;
   public final int i;
   public final int j;

   public GomokuAction(int i, int j) {
      this.i = i;
      this.j = j;
   }

   public String toString() {
      return this.i + " " + this.j;
   }
}
