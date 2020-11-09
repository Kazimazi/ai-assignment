package game.engine.utils;

import com.google.gson.Gson;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import javafx.scene.image.Image;

public final class Utils {
   private static final Gson gson = new Gson();
   private static Image[] images = null;

   public static String jsonSerialize(Object object) {
      return gson.toJson(object);
   }

   public static <T> T jsonDeSerialize(Class<T> clazz, String json) {
      return gson.fromJson(json, clazz);
   }

   public static Gson getGson() {
      return gson;
   }

   public static Image[] getImages(String pathPrefix) {
      if (images == null) {
         List<Image> imgs = new LinkedList();
         int idx = 0;

         InputStream is;
         do {
            is = Utils.class.getResourceAsStream(pathPrefix + idx + ".png");
            if (is != null) {
               imgs.add(new Image(is));
            }

            ++idx;
         } while(is != null);

         images = (Image[])imgs.toArray(new Image[0]);
      }

      return images;
   }

   public static Image[] getImages(String pathPrefix, String[] fNames) {
      if (images == null) {
         List<Image> imgs = new LinkedList();

         for(int i = 0; i < fNames.length; ++i) {
            InputStream is = Utils.class.getResourceAsStream(pathPrefix + fNames[i] + ".png");
            if (is != null) {
               imgs.add(new Image(is));
            }
         }

         images = (Image[])imgs.toArray(new Image[0]);
      }

      return images;
   }

   public static int[][] copy(int[][] array) {
      int[][] result = new int[array.length][];

      for(int i = 0; i < array.length; ++i) {
         result[i] = new int[array[i].length];
         System.arraycopy(array[i], 0, result[i], 0, array[i].length);
      }

      return result;
   }
}
