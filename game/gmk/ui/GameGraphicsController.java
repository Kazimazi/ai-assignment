package game.gmk.ui;

import game.gmk.GomokuAction;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class GameGraphicsController implements Initializable, EventHandler<MouseEvent> {
   private static final BlockingQueue<GomokuAction> QUEUE = new ArrayBlockingQueue(1, true);
   @FXML
   private Canvas gameLayer;
   @FXML
   private Canvas bgLayer;
   private GraphicsContext gameGC;
   private GraphicsContext bgGC;
   private int lastX = -1;
   private int lastY = -1;

   public void initialize(URL url, ResourceBundle rb) {
      this.bgLayer.toFront();
      this.gameLayer.toFront();
      this.gameGC = this.gameLayer.getGraphicsContext2D();
      this.bgGC = this.bgLayer.getGraphicsContext2D();
      this.bgGC.setFill(Color.DARKRED);
      this.gameLayer.setOnMouseClicked(this);
      this.gameLayer.setOnMouseMoved(this);
      this.gameLayer.setOnMouseExited(this);
   }

   public void setSize(int width, int height) {
      this.gameLayer.setWidth((double)width);
      this.bgLayer.setWidth((double)width);
      this.gameLayer.setHeight((double)height);
      this.bgLayer.setHeight((double)height);
   }

   void clear() {
      this.gameGC.clearRect(0.0D, 0.0D, this.gameLayer.getWidth(), this.gameLayer.getHeight());
   }

   void update(double x, double y, double width, double height, Image image) {
      this.gameGC.drawImage(image, x, y, width, height);
   }

   public synchronized void handle(MouseEvent event) {
      EventType<? extends MouseEvent> eventType = event.getEventType();
      int x = (int)event.getX();
      int y = (int)event.getY();
      if (MouseEvent.MOUSE_CLICKED.equals(eventType)) {
         this.onMouseClick(x, y);
      } else if (MouseEvent.MOUSE_MOVED.equals(eventType)) {
         if ((double)this.lastX != -1.0D && (double)this.lastY != -1.0D) {
            this.onMouseLeave(this.lastX, this.lastY);
         }

         this.onMouseMove(x, y);
      } else if (MouseEvent.MOUSE_EXITED.equals(eventType) && (double)this.lastX != -1.0D && (double)this.lastY != -1.0D) {
         this.onMouseLeave(this.lastX, this.lastY);
      }

   }

   public static GomokuAction getAction() throws InterruptedException {
      return (GomokuAction)QUEUE.take();
   }

   private void onMouseClick(int x, int y) {
      GomokuAction action = new GomokuAction(y / 30, x / 30);
      boolean succes = QUEUE.offer(action);
      if (!succes) {
         QUEUE.poll();
         succes = QUEUE.offer(action);
      }

   }

   private void onMouseMove(int x, int y) {
      this.lastX = x / 30 * 30;
      this.lastY = y / 30 * 30;
      this.bgGC.fillRect((double)this.lastX, (double)this.lastY, 30.0D, 30.0D);
   }

   private void onMouseLeave(int x, int y) {
      this.bgGC.clearRect((double)x, (double)y, 30.0D, 30.0D);
   }
}
