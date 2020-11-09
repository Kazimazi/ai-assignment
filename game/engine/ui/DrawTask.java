package game.engine.ui;

public final class DrawTask implements Runnable {
   private final Drawable drawable;

   public DrawTask(Drawable drawable) {
      this.drawable = drawable;
   }

   public void run() {
      this.drawable.getApplication().draw(this.drawable);
   }
}
