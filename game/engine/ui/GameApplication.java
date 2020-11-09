package game.engine.ui;

import java.util.concurrent.CountDownLatch;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public abstract class GameApplication extends Application {
   private static final CountDownLatch waiter = new CountDownLatch(1);
   private static GameApplication instance = null;

   public static final GameApplication getInstance() {
      try {
         waiter.await();
      } catch (InterruptedException var1) {
         var1.printStackTrace();
      }

      return instance;
   }

   public abstract void draw(Drawable var1);

   protected abstract Scene getScene();

   public final void start(Stage primaryStage) throws Exception {
      primaryStage.setTitle(this.getClass().getSimpleName());
      primaryStage.setScene(this.getScene());
      primaryStage.show();
      primaryStage.setResizable(false);
      instance = this;
      waiter.countDown();
   }

   public final void close() {
      Platform.exit();
   }
}
