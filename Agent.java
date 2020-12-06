//koffeintabletta,Koszo.Attila@stud.u-szeged.hu
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import game.gmk.GomokuAction;
import game.gmk.GomokuGame;
import game.gmk.GomokuPlayer;

public class Agent extends GomokuPlayer {
    /** Array for all kinda good actions might not be tho. */
    ArrayList<GomokuAction> actions = new ArrayList<GomokuAction>(); // make some kind of set for it

    // Threats
    // Winning
    int[] WINNINGFIVE = {5, 1};
    int[] OPENFOUR = {4, 2};
    // Forcing
    int[] SIMPLEFOUR = {4, 1};
    int[] OPENTHREE = {3, 3};
    int[] BROKENTHREE = {3, 2};
    // Non-forcing
    int[] SIMPLETHREE = {3, 1};
    /** Two that can be extended to five in 4 ways. */
    int[] TWOINFOUR = {2, 4};
    /** Two that can be extended to five in 3 ways. */
    int[] TWOINTHREE = {2, 3};
    /** Two that can be extended to five in 2 ways. */
    int[] TWOINTWO = {2, 2};
    /** Two that can be extended to five in 1 way. */
    int[] TWOINONE = {2, 1};
    /** One that can be extended to five in 5 ways. */
    int[] ONEINFIVE = {1, 5};
    /** One that can be extended to five in 4 ways. */
    int[] ONEINFOUR = {1, 4};
    /** One that can be extended to five in 3 ways. */
    int[] ONEINTHREE = {1, 3};
    /** One that can be extended to five in 2 ways. */
    int[] ONEINTWO = {1, 2};
    /** One that can be extended to five in 1 way. */
    int[] ONEINONE = {1, 1};
    /** No threat */
    int NOTHREAT = 0;

    /**
     * Creates muh player, sets the specified parameters to the super class.
     * @param color player's color
     * @param board game board
     * @param random random number generator
     * @see GomokuPlayer#GomokuPlayer(int, int[][], Random)
     */
    public Agent(int color, int[][] board, Random random) {
        super(color, board, random);
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                actions.add(new GomokuAction(i, j));
            }
        }
    }

    @Override
    public GomokuAction getAction(GomokuAction prevAction, long[] remainingTimes) {
        if (prevAction != null) {
            board[prevAction.i][prevAction.j] = 1 - color;
        }

        GomokuAction action = null;
        double score = Double.MIN_VALUE; // -Inf

        for (GomokuAction a : actions) {
            if (board[a.i][a.j] == GomokuGame.EMPTY) {
                double s = scoreAction(board, a, color) + 0.9 *  scoreAction(board, a, 1 - color);
                if (score < s) {
                    score = s;
                    action = a;
                }
            }
        }
        board[action.i][action.j] = color;

        return action;
    }

    int[][] boardAfterAction(int[][] board, GomokuAction action, int color) {
        board[action.i][action.j] = color;
        return board;
    }

    /**
     * From the given point to a given direction counts the consecutive non WALL/ENEMY tiles.
     * @param board that we observe
     * @param i row of start position
     * @param j column of start position
     * @param di row direction
     * @param dj column direction
     * @param c color of player
     * @param timesCalled times it's called, if reaches 5 terminate and returns
     * @param cCounter how many player colored tiles we found
     * @return 0 if stepped on obstacle, 1-5 amount of player color tiles
     */
    int countDirection2(int[][] board, int i, int j, int di, int dj, int c, int timesCalled, int cCounter) {
        int ni = (i + board.length + di) % board.length;
        int nj = (j + board[ni].length + dj) % board[ni].length;
        if (board[ni][nj] == GomokuGame.WALL || board[ni][nj] == 1 - c) {
            return 0;
        } else if (board[ni][nj] == c) {
            cCounter++;
        }
        if (++timesCalled == 5) return cCounter;
        return countDirection2(board, ni, nj, di, dj, c, timesCalled, cCounter);
    }

    ArrayList<Integer> getThreats(int[][] board, GomokuAction a, int c) {
        int[][] boardAfterAction = boardAfterAction(GomokuGame.copy(board), a, c);

        int[] horizontalThreat = {0, 0};
        int[] verticalThreat = {0, 0};
        int[] diagonalThreat1 = {0, 0};
        int[] diagonalThreat2 = {0, 0};

        // From left to right
        for (int j = a.j - 4; j <= a.j; j++) {
            int sameColors = countDirection2(boardAfterAction, a.i, j - 1, 0, 1, c, 0, 0);
            if (sameColors == horizontalThreat[0]) {
                horizontalThreat[0] = sameColors;
                horizontalThreat[1]++;
            } else if (sameColors > horizontalThreat[0]){
                horizontalThreat[0] = sameColors;
                horizontalThreat[1] = 1;
            }
        }
        // From top to bottom
        for (int i = a.i - 4; i <= a.i; i++) {
            int sameColors = countDirection2(boardAfterAction, i - 1, a.j, 1, 0, c, 0, 0);
            if (sameColors == verticalThreat[0]) {
                verticalThreat[0] = sameColors;
                verticalThreat[1]++;
            } else if (sameColors > verticalThreat[0]){
                verticalThreat[0] = sameColors;
                verticalThreat[1] = 1;
            }
        }
        // From top left to bottom right
        for (int i = a.i - 4, j = a.j - 4; i <= a.i && j <= a.j; i++, j++) {
            int sameColors = countDirection2(boardAfterAction, i - 1, j - 1, 1, 1, c, 0, 0);
            if (sameColors == diagonalThreat1[0]) {
                diagonalThreat1[0] = sameColors;
                diagonalThreat1[1]++;
            } else if (sameColors > diagonalThreat1[0]){
                diagonalThreat1[0] = sameColors;
                diagonalThreat1[1] = 1;
            }
        }
        // From top right to bottom left
        for (int i = a.i - 4, j = a.j + 4; i <= a.i && j >= a.j; i++, j--) {
            int sameColors = countDirection2(boardAfterAction, i - 1, j + 1, 1, -1, c, 0, 0);
            if (sameColors == diagonalThreat2[0]) {
                diagonalThreat2[0] = sameColors;
                diagonalThreat2[1]++;
            } else if (sameColors > diagonalThreat2[0]){
                diagonalThreat2[0] = sameColors;
                diagonalThreat2[1] = 1;
            }
        }

        ArrayList<Integer> threats = new ArrayList<Integer>();
        threats.add(scoreThreat(horizontalThreat, a, "h"));
        threats.add(scoreThreat(verticalThreat, a, "v"));
        threats.add(scoreThreat(diagonalThreat1, a, "d1"));
        threats.add(scoreThreat(diagonalThreat2, a, "d2"));

        return threats;
    }

    /**
     * Calculates score of an action.
     * @param board that we observe
     * @param a is the given action
     * @param c color of player
     * @return score worthy (or not) of a best action
     */
    double scoreAction(int[][] board, GomokuAction a, int c) {
        ArrayList<Integer> threats = getThreats(board, a, c);

        Collections.sort(threats);
        int bestScore = threats.get(3);
        int secondBestScore = threats.get(2);

        double calculatedScore = 1.5 * Math.pow(1.8, bestScore) + Math.pow(1.8, secondBestScore);

        return calculatedScore;
    }

    /** My ambigous scoring for a certain treat */
    int scoreThreat(int[] threat, GomokuAction a, String s) {
        int score = 0;
        if (Arrays.equals(threat, WINNINGFIVE))
            score = 16;
        else if (Arrays.equals(threat, OPENFOUR))
            score = 15;
        else if (Arrays.equals(threat, SIMPLEFOUR))
            score = 14;
        else if (Arrays.equals(threat, OPENTHREE))
            score = 13;
        else if (Arrays.equals(threat, BROKENTHREE))
            score = 12;
        else if (Arrays.equals(threat, SIMPLETHREE))
            score = 11;
        else if (Arrays.equals(threat, TWOINFOUR))
            score = 10;
        else if (Arrays.equals(threat, TWOINTHREE))
            score = 9;
        else if (Arrays.equals(threat, TWOINTWO))
            score = 8;
        else if (Arrays.equals(threat, TWOINONE))
            score = 7;
        else if (Arrays.equals(threat, ONEINFIVE))
            score = 6;
        else if (Arrays.equals(threat, ONEINFOUR))
            score = 5;
        else if (Arrays.equals(threat, ONEINTHREE))
            score = 4;
        else if (Arrays.equals(threat, ONEINTWO))
            score = 3;
        else if (Arrays.equals(threat, ONEINONE))
            score = 2;
        return score;
    }
}
