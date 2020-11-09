package game.engine.utils;

import java.io.Serializable;

public class Pair<F, S> implements Serializable {
   private static final long serialVersionUID = -6613238923067231223L;
   public final F first;
   public final S second;

   public Pair(F first, S second) {
      this.first = first;
      this.second = second;
   }

   public String toString() {
      return Utils.jsonSerialize(this);
   }
}
