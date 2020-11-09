package game.engine.utils;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;

public class ConstructionTask<R> implements TimeOutTask<R> {
   private Constructor<R> constructor;
   private Object[] params;
   private long elapsed;

   public void setConstructor(Constructor<R> constructor, Object... params) {
      this.constructor = constructor;
      this.params = params;
   }

   public long getElapsed() {
      return this.elapsed;
   }

   public R call() throws Exception {
      long start = ManagementFactory.getThreadMXBean().getThreadUserTime(Thread.currentThread().getId());
      R result = this.constructor.newInstance(this.params);
      this.elapsed = ManagementFactory.getThreadMXBean().getThreadUserTime(Thread.currentThread().getId()) - start;
      return result;
   }
}
