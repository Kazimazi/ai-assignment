package game.engine.ui;

import java.util.List;
import javafx.application.Application;

public interface Drawable {
   Class<? extends Application> getApplicationClass();

   List<GameObject> getGameObjects();

   GameApplication getApplication();

   String[] getAppParams();
}
