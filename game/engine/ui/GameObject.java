package game.engine.ui;

import javafx.scene.image.Image;

public class GameObject {
   public final double x;
   public final double y;
   public final double width;
   public final double height;
   public final Image image;

   public GameObject(double x, double y, double width, double height, Image image) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.image = image;
   }
}
