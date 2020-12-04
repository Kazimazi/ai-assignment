import java.util.ArrayList;
import java.util.Random;

import game.gmk.GomokuAction;
import game.gmk.GomokuGame;
import game.gmk.GomokuPlayer;

public class Agent extends GomokuPlayer {
    /** Array for all kinda good actions might not be tho. */
    protected ArrayList<GomokuAction> actions = new ArrayList<GomokuAction>(); // make some kind of set for it

    private boolean LOG = false;

    /**
     * Creates muh player, sets the specified parameters to the super class.
     * @param color player's color
     * @param board game board
     * @param random random number generator
     * @see GomokuPlayer#GomokuPlayer(int, int[][], Random)
     */
    public Agent(int color, int[][] board, Random random) {
        super(color, board, random);
    }

    @Override
    public GomokuAction getAction(GomokuAction prevAction, long[] remainingTimes) {
        if (prevAction == null) {
            // TODO you can do it better yeah?
            int i = board.length / 2;
            int j = board[i].length / 2;
            while (board[i][j] != GomokuGame.EMPTY) {
                i = random.nextInt(board.length);
                j = random.nextInt(board[i].length);
            }
            takeAction(i, j, color);
            return new GomokuAction(i, j);
        }

        takeAction(prevAction.i, prevAction.j, 1 - color);

        GomokuAction action = null; // init best move
        int score = Integer.MIN_VALUE; // -Inf
        for (GomokuAction a : actions) {
            if (board[a.i][a.j] == GomokuGame.EMPTY) {
                int s = score(a.i, a.j, color) + score(a.i, a.j, 1 - color);
                //int s = negamax(board, 2, color);
                if (score < s) {
                    score = s;
                    action = a;
                }
            }
        }

        takeAction(action.i, action.j, color);

        return action;
    }

    /**
     * Takes a move but doesn't commit to it.
     * @param a given move
     * @param color player's color
     * @return new board with given action.
     */
    protected int[][] tryAction(GomokuAction a, int color) {
        int[][] newBoard = GomokuGame.copy(board);
        newBoard[a.i][a.j] = color;
        return newBoard;
    }

    /**
     * Change state of board and actions.
     * @param i
     * @param j
     * @param color
     */
    protected void takeAction(int i, int j, int color) {
        // color the board
        board[i][j] = color;

        // remove taken action from actions
        if (!actions.isEmpty()) {
            int index = -1;
            //Log("Not empty action.");
            //Log("ac " + i + " " + j);
            for (GomokuAction action : actions) {
                //Log("cy " + action);
                if (action.i == i && action.j == j) {
                    index = actions.indexOf(action);
                }
            }
            Log(">Remove action: " + actions.get(index));
            actions.remove(index);
        }

        int distance = 2;
        for (int ni = i - distance; ni != i + distance + 1; ni++) {
            int realI = (ni + board.length) % board.length;
            for (int nj = j - distance; nj != j + distance + 1; nj++) {
                int realJ = (nj + board[realI].length) % board[realI].length;
                boolean isNotSameRowColumn = !(realI == i && realJ == j);
                Log("Do we add? " + "realI: " + realI + " realJ: " + realJ + " > " + String.valueOf(isNotSameRowColumn));
                // ha nem ugyan abban a sorban/oszlop parban vannak es
                // ha az egy ures mezo a tablan es
                // ha meg nincs benne az actionjainkben
                if (isNotSameRowColumn && board[realI][realJ] == GomokuGame.EMPTY) {
                    boolean isAlreadyInActions = false;
                    for (GomokuAction a : actions) {
                        if (a.i == realI && a.j == realJ)
                            isAlreadyInActions = true;
                    }
                    if (!isAlreadyInActions) {
                        GomokuAction action = new GomokuAction(realI, realJ);
                        actions.add(action);
                        Log(">Add action: " + action);
                    }
                }
            }
        }
        Log("Actions: " + actions);
    }

    //protected ArrayList<int[][]> potentialBoards(int[][] board, int color) {
    //    ArrayList<int[][]> boards = new ArrayList<>();
    //    for (GomokuAction action : actions) {
    //        if (board[action.i][action.j] == GomokuGame.EMPTY) {
    //            board[action.i][action.j] = color; // do the move
    //            boards.add(board);
    //            board[action.i][action.j] = GomokuGame.EMPTY; // take the move back
    //        }
    //    };
    //    return boards;
    //}

    //protected int negamax(int[][] board, int depth, int player) {
    //    if (depth == 0 || GomokuGame.hasFive(board, color))
    //        return color * Integer.MAX_VALUE; // TODO color x heuristic value of board
    //    int value = Integer.MIN_VALUE;
    //    for (int[][] child : potentialBoards(board, color)) {
    //        value = Integer.max(value, -negamax(child, depth - 1, -color));
    //    }
    //    return value;
    //}

    /**
     * The score of a cell is the aggregated value of the any direction from
     * there computed by {@link GreedyPlayer#countDirection(int, int, int, int, int)} function.
     * @param i row of the cell
     * @param j column of the cell
     * @param c color
     * @return aggregated neighbor score
     */
    protected int score(int i, int j, int c) {
        // TODO check if line is sealed on one end, it would worth less
        int result = 0;
        // right up
        result += 1 << countDirection(i, j, 1, -1, c);
        // right
        result += 1 << countDirection(i, j, 1, 0, c);
        // right down
        result += 1 << countDirection(i, j, 1, 1, c);
        // down
        result += 1 << countDirection(i, j, 0, 1, c);
        // left down
        result += 1 << countDirection(i, j, -1, 1, c);
        // left
        result += 1 << countDirection(i, j, -1, 0, c);
        // left up
        result += 1 << countDirection(i, j, -1, -1, c);
        // up
        result += 1 << countDirection(i, j, 0, -1, c);

        //Log("score result: " + result);

        return result;
    }

    /**
     * Counts the number of consecutive cells from the specified start point at
     * the specified direction belongs to the specified color.
     * @param i row of start position
     * @param j column of start position
     * @param di row direction
     * @param dj column direction
     * @param c color
     * @return number of consecutive cells
     */
    protected int countDirection(int i, int j, int di, int dj, int c) {
        int ni = (i + board.length + di) % board.length;
        int nj = (j + board[ni].length + dj) % board[ni].length;
        if (board[ni][nj] != c) {
            return 0;
        }
        return 1 + countDirection(ni, nj, di, dj, c);
    }

    private void Log(String string) {
        if (LOG)
            System.out.println("~~~" + string);
    }
}
