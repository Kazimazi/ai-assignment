package game.engine.utils;

import java.util.concurrent.Callable;

public interface TimeOutTask<R> extends Callable<R> {
   long getElapsed();
}
