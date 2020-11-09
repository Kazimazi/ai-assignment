package game.engine;

import game.engine.utils.Pair;
import java.util.List;

public interface Player<A extends Action> {
   A getAction(List<Pair<Integer, A>> var1, long[] var2);

   int getColor();
}
