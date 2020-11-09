package game.engine.utils;

import game.engine.Action;
import game.engine.Player;
import java.lang.management.ManagementFactory;
import java.util.List;

public final class ActionTask implements TimeOutTask<Action> {
   private Player<Action> player;
   private List<Pair<Integer, Action>> prevAction;
   private long elapsed;
   private long[] remainings;

   public void setParams(Player<Action> player, List<Pair<Integer, Action>> prevAction, long[] remainings) {
      this.player = player;
      this.prevAction = prevAction;
      this.remainings = remainings;
   }

   public long getElapsed() {
      return this.elapsed;
   }

   public Action call() throws Exception {
      long start = ManagementFactory.getThreadMXBean().getThreadUserTime(Thread.currentThread().getId());
      Action result = this.player.getAction(this.prevAction, this.remainings);
      this.elapsed = ManagementFactory.getThreadMXBean().getThreadUserTime(Thread.currentThread().getId()) - start;
      return result;
   }
}
