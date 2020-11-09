package game.engine;

import com.google.gson.reflect.TypeToken;
import game.engine.ui.DrawTask;
import game.engine.ui.Drawable;
import game.engine.ui.GameApplication;
import game.engine.utils.ActionTask;
import game.engine.utils.ConstructionTask;
import game.engine.utils.Pair;
import game.engine.utils.StringBufferOutputStream;
import game.engine.utils.TimeOutTask;
import game.engine.utils.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javafx.application.Application;
import javafx.application.Platform;

public final class Engine {
   private static boolean isDebug;
   private final double fps;
   private final Game<Player<Action>, Action> game;
   private final Player<Action>[] players;
   private final List<Pair<Integer, Action>>[] prevActions;
   private final long[] remainingTimes;
   private final long[][] playerRemainingTimes;
   private static final PrintStream defaultOut;
   private static final PrintStream defaultErr;
   private static final StringBuffer sbOut;
   private static final StringBuffer sbErr;
   private static final PrintStream userOut;
   private static final PrintStream userErr;
   private static final ActionTask actionTask;
   private static final ExecutorService service;
   private final Type logType;
   private final String ofName;
   private PrintWriter os;
   private BufferedReader is;
   private boolean isReplay = false;
   private static final String LOGEND = "LOGEND";

   public Engine(double fps, String gameClass, String[] params) throws Exception {
      long postfix = System.nanoTime() % 1000000000L;
      this.fps = fps;
      isDebug = 0.0D < fps;
      File f = new File(gameClass);
      this.ofName = "gameplay_" + postfix + ".data";
      if (f.exists()) {
         this.is = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf8"));
         gameClass = (String)Utils.getGson().fromJson(this.is.readLine(), String.class);
         params = (String[])Utils.getGson().fromJson(this.is.readLine(), String[].class);
         this.isReplay = true;
      } else if (!isDebug) {
         this.os = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.ofName), "utf8"), true);
         this.os.println(Utils.getGson().toJson((Object)gameClass));
         this.os.println(Utils.getGson().toJson((Object)params));
      }

      if (!isDebug) {
         System.setOut(userOut);
         System.setErr(userErr);
      }

      this.game = (Game)Class.forName(gameClass).getConstructor(PrintStream.class, String[].class, Boolean.TYPE).newInstance(defaultErr, params, this.isReplay);
      this.logType = TypeToken.getParameterized(Pair.class, this.game.getActionClass(), Long.class).getType();
      this.players = this.game.getPlayers();
      this.prevActions = new List[this.players.length];
      this.remainingTimes = new long[this.players.length];
      this.playerRemainingTimes = new long[this.players.length][this.players.length];

      for(int i = 0; i < this.players.length; ++i) {
         this.prevActions[i] = new LinkedList();
         this.remainingTimes[i] = this.game.getRemainingTime(this.players[i]);

         for(int j = 0; j < this.playerRemainingTimes.length; ++j) {
            this.playerRemainingTimes[j][i] = this.remainingTimes[i];
         }
      }

      if (isDebug) {
         defaultOut.println("GAME: " + gameClass);
         defaultOut.println("PARAMETERS: " + Arrays.toString(params));
      }

   }

   public void play() throws Exception {
      GameApplication gameApplication = null;
      DrawTask drawTask = null;
      boolean isDrawable = this.game instanceof Drawable;

      try {
         Class.forName("javafx.application.Application");
      } catch (ClassNotFoundException var12) {
         isDrawable = false;
      }

      if (isDebug) {
         defaultOut.println(this.game);
         if (isDrawable) {
            Drawable drawable = (Drawable)this.game;
            gameApplication = drawable.getApplication();
            drawTask = new DrawTask(drawable);
            Platform.runLater(drawTask);
         }
      }

      while(!this.game.isFinished()) {
         Player<Action> currentPlayer = this.game.getNextPlayer();
         if (currentPlayer == null) {
            break;
         }

         this.remainingTimes[currentPlayer.getColor()] = this.game.getRemainingTime(currentPlayer);

         for(int i = 0; i < this.remainingTimes.length; ++i) {
            this.playerRemainingTimes[currentPlayer.getColor()][i] = this.remainingTimes[i];
         }

         if (isDebug) {
            defaultOut.println("CURRENT: " + currentPlayer + " SCORE: " + this.game.getScore(currentPlayer) + " REM.TIME: " + this.remainingTimes[currentPlayer.getColor()] + " ns");
         }

         Pair<Action, Long> result = null;
         List<Pair<Integer, Action>> prevAction = this.prevActions[currentPlayer.getColor()];
         if (this.isReplay) {
            String line = this.is.readLine();
            if (line.equals("LOGEND")) {
               break;
            }

            result = (Pair)Utils.getGson().fromJson(line, this.logType);
         } else {
            actionTask.setParams(currentPlayer, prevAction, this.playerRemainingTimes[currentPlayer.getColor()]);
            result = timeOutTask(actionTask, this.remainingTimes[currentPlayer.getColor()] + 1L);
         }

         Action currentAction = (Action)result.first;
         long elapsed = (Long)result.second;
         prevAction.clear();

         for(int i = 0; i < this.prevActions.length; ++i) {
            if (i != currentPlayer.getColor()) {
               this.prevActions[i].add(new Pair(currentPlayer.getColor(), currentAction));
            }
         }

         if (!this.isReplay && !isDebug) {
            this.os.println(Utils.getGson().toJson((Object)result));
         }

         if (isDebug) {
            defaultOut.println("ACTION: " + currentAction);
            defaultOut.println("ELAPSED TIME: " + elapsed + " ns");
            if (!this.game.isValid(currentAction)) {
               defaultOut.println("ACTION: " + currentAction + " IS NOT VALID!!!");
            }
         }

         this.game.setAction(currentPlayer, currentAction, elapsed);
         if (isDebug) {
            defaultOut.println(this.game);
            if (isDrawable) {
               Platform.runLater(drawTask);
            }

            try {
               Thread.sleep((long)(1000.0D / this.fps));
            } catch (InterruptedException var11) {
               var11.printStackTrace(defaultErr);
            }
         }
      }

      service.shutdown();
      if (isDebug && isDrawable) {
         gameApplication.close();
      }

      System.setOut(defaultOut);
      System.setErr(defaultErr);
      if (this.isReplay) {
         this.is.close();
      } else if (!isDebug) {
         this.os.println("LOGEND");
         this.os.close();
         defaultOut.println("logfile: " + this.ofName);
      }

      for(int i = 0; i < this.players.length; ++i) {
         defaultOut.println(i + " " + this.players[i] + " " + (this.players[i] == null ? i + " " : "") + this.game.getScore(this.players[i]) + " " + this.game.getRemainingTime(this.players[i]));
      }

   }

   public static void main(String[] args) throws Exception {
      if (args.length < 2) {
         System.err.println("required parameters for the engine are:");
         System.err.println("\t- debug/fps      : integer debug parameter (0: no debug, 0 < : game speed (frames per sec))");
         System.err.println("\t- game class     : class of the game to be run");
         System.err.println("\t- game parameters: the parameters of the specified game");
         System.exit(1);
      }

      double fps = Double.parseDouble(args[0]);
      if (fps < 0.0D) {
         System.err.println("Negative value is forbidden for fps: " + fps);
         System.exit(1);
      }

      String gameClass = args[1];
      String[] params = (String[])Arrays.copyOfRange(args, 2, args.length);
      Engine engine = null;

      try {
         engine = new Engine(fps, gameClass, params);
      } catch (Exception var10) {
         var10.printStackTrace(defaultErr);
         service.shutdown();
         return;
      }

      Game<Player<Action>, Action> game = engine.game;
      Thread drawThread = null;
      if (isDebug && game instanceof Drawable) {
         try {
            Class.forName("javafx.application.Application");
            final Drawable drawable = (Drawable)game;
            drawThread = new Thread(new Runnable() {
               public void run() {
                  Application.launch(drawable.getApplicationClass(), drawable.getAppParams());
               }
            });
            drawThread.start();
         } catch (ClassNotFoundException var9) {
         }
      }

      engine.play();
      if (drawThread != null && isDebug && game instanceof Drawable) {
         drawThread.join();
      }

      System.exit(0);
   }

   public static final <R> Pair<R, Long> timeOutTask(TimeOutTask<R> task, long timeout) {
      Future<R> future = service.submit(task);
      R result = null;
      long elapsed = 0L;

      try {
         result = future.get(timeout + 1L, TimeUnit.NANOSECONDS);
         elapsed = task.getElapsed();
      } catch (TimeoutException var12) {
         defaultOut.println("TIME HAS RUN OUT!!!");
         elapsed = timeout + 1L;
      } catch (Throwable var13) {
         var13.printStackTrace(defaultErr);
         elapsed = timeout + 1L;
      } finally {
         future.cancel(true);
      }

      if (!isDebug && (sbOut.length() > 0 || sbErr.length() > 0)) {
         elapsed = timeout + 1L;
         cleanOut();
      }

      return new Pair(result, elapsed);
   }

   public static final <R> Pair<R, Long> construct(long timeout, Constructor<R> constructor, Object... params) {
      ConstructionTask<R> task = new ConstructionTask();
      task.setConstructor(constructor, params);
      Pair<R, Long> result = timeOutTask(task, timeout + 1L);
      return result;
   }

   private static final void cleanOut() {
      defaultErr.println("Writing is forbidden!");
      defaultErr.println("USER.OUT:\n" + sbOut);
      defaultErr.println("USER.ERR:\n" + sbErr);
      sbOut.delete(0, sbOut.length());
      sbErr.delete(0, sbErr.length());
   }

   static {
      defaultOut = System.out;
      defaultErr = System.err;
      sbOut = new StringBuffer();
      sbErr = new StringBuffer();
      userOut = new PrintStream(new StringBufferOutputStream(sbOut));
      userErr = new PrintStream(new StringBufferOutputStream(sbErr));
      actionTask = new ActionTask();
      service = Executors.newCachedThreadPool();
      ManagementFactory.getThreadMXBean().setThreadCpuTimeEnabled(true);
   }
}
