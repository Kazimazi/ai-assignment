package game.engine;

public interface Game<P extends Player<A>, A extends Action> {
   P[] getPlayers();

   P getNextPlayer();

   boolean isValid(A var1);

   void setAction(P var1, A var2, long var3);

   long getRemainingTime(P var1);

   boolean isFinished();

   double getScore(P var1);

   Class<? extends Action> getActionClass();
}
