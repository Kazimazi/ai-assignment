package game.gmk;

import game.engine.Action;
import game.engine.Engine;
import game.engine.Game;
import game.engine.ui.Drawable;
import game.engine.ui.GameApplication;
import game.engine.ui.GameObject;
import game.engine.utils.Pair;
import game.engine.utils.Utils;
import game.gmk.players.DummyPlayer;
import game.gmk.players.HumanPlayer;
import game.gmk.ui.GomokuApplication;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import javafx.application.Application;
import javafx.scene.image.Image;

public class GomokuGame implements Game<GomokuPlayer, GomokuAction>, Drawable {
   public static final int COLOR_X = 0;
   public static final int COLOR_O = 1;
   public static final int EMPTY = 2;
   public static final int WALL = 3;
   public static final HashMap<Integer, String> TILES = new HashMap();
   private final long seed;
   private final int n;
   private final int m;
   private final int[][] board;
   private final GomokuPlayer[] players;
   private final long[] remainingTimes;
   private final int[] score;
   private int currentPlayer;
   private int emptyTiles;
   private boolean finished = false;
   private GomokuAction lastAction;
   private final PrintStream errStream;

   public GomokuGame(PrintStream errStream, String[] params, boolean isReplay) throws Exception {
      if (params.length != 7) {
         errStream.println("required parameters for the game are:");
         errStream.println("\t- random seed       : controlls the sequence of the random numbers");
         errStream.println("\t- num of rows (n)   : number of board rows");
         errStream.println("\t- num of columns (m): number of board columns");
         errStream.println("\t- wall probability  : probability of a cell to be a wall");
         errStream.println("\t- timeout           : play-time for a player in milliseconds");
         errStream.println("\t- player classes... : list of player classes (exactly 2)");
         System.exit(1);
      }

      this.errStream = errStream;
      this.seed = Long.parseLong(params[0]);
      this.n = Integer.parseInt(params[1]);
      this.m = Integer.parseInt(params[2]);
      double wallProb = Double.parseDouble(params[3]);
      long time = Long.parseLong(params[4]) * 1000000L;
      Random r = new Random(this.seed);
      this.board = new int[this.n][this.m];

      int i;
      for(i = 0; i < this.n; ++i) {
         for(int j = 0; j < this.m; ++j) {
            this.board[i][j] = r.nextDouble() < wallProb ? 3 : 2;
            if (this.board[i][j] == 2) {
               ++this.emptyTiles;
            }
         }
      }

      this.players = new GomokuPlayer[2];
      this.remainingTimes = new long[this.players.length];
      this.score = new int[this.players.length];

      for(i = 0; i < this.players.length; ++i) {
         r = new Random(this.seed);
         Class<? extends GomokuPlayer> clazz = Class.forName(DummyPlayer.class.getName()).asSubclass(GomokuPlayer.class);
         if (isReplay) {
            errStream.println("Game is in replay mode, Player: " + i + " is the DummyPlayer, but was: " + params[i + 5]);
         } else {
            clazz = Class.forName(params[i + 5]).asSubclass(GomokuPlayer.class);
         }

         Constructor<? extends GomokuPlayer> constructor = clazz.getConstructor(Integer.TYPE, int[][].class, Random.class);
         Pair<? extends GomokuPlayer, Long> created = Engine.construct(time, constructor, i, copy(this.board), r);
         this.players[i] = (GomokuPlayer)created.first;
         this.score[i] = 0;
         if (this.players[i] instanceof HumanPlayer) {
            this.remainingTimes[i] = Long.MAX_VALUE - (Long)created.second - 10L;
         } else {
            this.remainingTimes[i] = time - (Long)created.second;
         }

         if (this.players[i].color != i) {
            int color = this.players[i].color;
            this.remainingTimes[i] = 0L;
            Field field = GomokuPlayer.class.getDeclaredField("color");
            field.setAccessible(true);
            field.set(this.players[i], i);
            field.setAccessible(false);
            errStream.println("Illegal color (" + color + ") was set for player: " + this.players[i]);
         }
      }

      this.currentPlayer = 0;
   }

   public GomokuPlayer[] getPlayers() {
      return this.players;
   }

   public GomokuPlayer getNextPlayer() {
      return this.players[this.currentPlayer];
   }

   public boolean isValid(GomokuAction action) {
      return isValid(this.board, action);
   }

   public void setAction(GomokuPlayer player, GomokuAction action, long time) {
      long[] var10000 = this.remainingTimes;
      int var10001 = player.color;
      var10000[var10001] -= time;
      if (this.remainingTimes[player.color] < 0L) {
         this.score[player.color] = -this.emptyTiles;
         this.score[this.players.length - player.color - 1] = this.emptyTiles;
         this.finished = true;
      } else {
         if (isValid(this.board, action)) {
            this.board[action.i][action.j] = player.color;
            --this.emptyTiles;
            this.currentPlayer = (this.currentPlayer + 1) % this.players.length;
            this.lastAction = action;
         } else {
            this.errStream.println("INVALID ACTION: " + action.toString() + ", OF: " + player.toString());
            this.score[player.color] = -this.emptyTiles;
            this.score[this.players.length - player.color - 1] = this.emptyTiles;
            this.finished = true;
         }

         if (this.emptyTiles == 0) {
            this.score[player.color] = 0;
            this.score[this.players.length - player.color - 1] = 0;
            this.finished = true;
         }

         if (hasFive(this.board, player.color)) {
            this.score[player.color] = this.emptyTiles;
            this.score[this.players.length - player.color - 1] = -this.emptyTiles;
            this.finished = true;
         }

      }
   }

   public long getRemainingTime(GomokuPlayer player) {
      return this.remainingTimes[player.color];
   }

   public boolean isFinished() {
      return this.finished;
   }

   public double getScore(GomokuPlayer player) {
      return (double)this.score[player.color];
   }

   public Class<? extends Action> getActionClass() {
      return GomokuAction.class;
   }

   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("    ");

      int i;
      for(i = 0; i < this.m; ++i) {
         sb.append(i / 10 == 0 ? "  " : i / 10 + " ");
      }

      sb.append("\n    ");

      for(i = 0; i < this.m; ++i) {
         sb.append(i % 10 + " ");
      }

      sb.append("\n");
      sb.append("   *");

      for(i = 0; i < this.m; ++i) {
         sb.append("--");
      }

      sb.append("*\n");

      for(i = 0; i < this.n; ++i) {
         sb.append(String.format(Locale.US, "%-3d", i));
         sb.append("|");

         for(int j = 0; j < this.m; ++j) {
            sb.append((String)TILES.get(this.board[i][j]));
         }

         sb.append("|\n");
      }

      sb.append("   *");

      for(i = 0; i < this.m; ++i) {
         sb.append("--");
      }

      sb.append("*\n");
      return sb.toString();
   }

   public static int[][] copy(int[][] a) {
      int[][] result = new int[a.length][];

      for(int i = 0; i < a.length; ++i) {
         result[i] = Arrays.copyOf(a[i], a[i].length);
      }

      return result;
   }

   public static final boolean isValid(int[][] board, GomokuAction action) {
      return action != null && isValid(board, action.i, action.j);
   }

   public static final boolean isValid(int[][] board, int i, int j) {
      return 0 <= i && i < board.length && 0 <= j && j < board[i].length && board[i][j] == 2;
   }

   public static final boolean hasFive(int[][] board, int color) {
      for(int i = 0; i < board.length; ++i) {
         for(int j = 0; j < board[i].length; ++j) {
            boolean result = true;

            int cj;
            int k;
            for(k = 0; k < 5; ++k) {
               cj = (j + k) % board[i].length;
               result = result && board[i][cj] == color;
            }

            if (result) {
               return result;
            }

            result = true;

            int ci;
            for(k = 0; k < 5; ++k) {
               ci = (i + k) % board.length;
               cj = (j + k) % board[ci].length;
               result = result && board[ci][cj] == color;
            }

            if (result) {
               return result;
            }

            result = true;

            for(k = 0; k < 5; ++k) {
               ci = (i + k) % board.length;
               result = result && board[ci][j] == color;
            }

            if (result) {
               return result;
            }

            result = true;

            for(k = 0; k < 5; ++k) {
               ci = (i + k) % board.length;
               cj = (j + board[ci].length - k) % board[ci].length;
               result = result && board[ci][cj] == color;
            }

            if (result) {
               return result;
            }
         }
      }

      return false;
   }

   public Class<? extends Application> getApplicationClass() {
      return GomokuApplication.class;
   }

   public List<GameObject> getGameObjects() {
      Image[] images = Utils.getImages("/game/gmk/ui/resources/", new String[]{"X", "O", "cell-0000007f", "cell-0000003f"});
      LinkedList<GameObject> gos = new LinkedList();
      if (this.lastAction != null) {
         gos.add(new GameObject((double)this.lastAction.j, (double)this.lastAction.i, 1.0D, 1.0D, images[3]));
      }

      for(int i = 0; i < this.n; ++i) {
         for(int j = 0; j < this.m; ++j) {
            if (this.board[i][j] == 0) {
               gos.add(new GameObject((double)j, (double)i, 1.0D, 1.0D, images[0]));
            }

            if (this.board[i][j] == 1) {
               gos.add(new GameObject((double)j, (double)i, 1.0D, 1.0D, images[1]));
            }

            if (this.board[i][j] == 3) {
               gos.add(new GameObject((double)j + 0.1D, (double)i + 0.1D, 0.8D, 0.8D, images[2]));
            }
         }
      }

      return gos;
   }

   public GameApplication getApplication() {
      return GameApplication.getInstance();
   }

   public String[] getAppParams() {
      return new String[]{"" + this.n, "" + this.m};
   }

   static {
      TILES.put(0, "X ");
      TILES.put(1, "O ");
      TILES.put(2, ". ");
      TILES.put(3, "# ");
   }
}
