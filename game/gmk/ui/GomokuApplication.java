package game.gmk.ui;

import game.engine.ui.Drawable;
import game.engine.ui.GameApplication;
import game.engine.ui.GameObject;
import java.util.Iterator;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

public class GomokuApplication extends GameApplication {
   public static final int MULTIPLIER = 30;
   private GameGraphicsController controller;

   public Scene getScene() {
      List<String> raw = this.getParameters().getRaw();
      int height = Integer.parseInt((String)raw.get(0)) * 30;
      int width = Integer.parseInt((String)raw.get(1)) * 30;
      FXMLLoader loader = new FXMLLoader(GomokuApplication.class.getResource("/game/gmk/ui/resources/GameGraphicsView.fxml"));
      Pane root = new Pane();

      try {
         root = (Pane)loader.load();
         root.setStyle("-fx-background-color: white,\n    linear-gradient(from 0.5px 0px to 15.5px 0px, repeat, gray 2%, transparent 5%),\n    linear-gradient(from 0px 0.5px to 0px 15.5px, repeat, gray 2%, transparent 5%);");
         this.controller = (GameGraphicsController)loader.getController();
         this.controller.setSize(width, height);
      } catch (Exception var7) {
         var7.printStackTrace();
      }

      return new Scene(root, (double)width, (double)height);
   }

   public void draw(Drawable drawable) {
      this.controller.clear();
      Iterator var2 = drawable.getGameObjects().iterator();

      while(var2.hasNext()) {
         GameObject go = (GameObject)var2.next();
         this.controller.update(go.x * 30.0D, go.y * 30.0D, go.width * 30.0D, go.height * 30.0D, go.image);
      }

   }
}
