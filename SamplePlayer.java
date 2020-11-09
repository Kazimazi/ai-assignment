import java.util.Random;
import game.gmk.GomokuAction;
import game.gmk.GomokuPlayer;

public class SamplePlayer extends GomokuPlayer {
    public SamplePlayer(int color, int[][] board, Random random)
    {
        super(color, board, random);
    }

    @Override
    public GomokuAction getAction(GomokuAction prevAction, long[] remainingTimes) {
        int i = random.nextInt(board.length);
        int j = random.nextInt(board[i].length);
        return new GomokuAction(i, j);
    }
}
