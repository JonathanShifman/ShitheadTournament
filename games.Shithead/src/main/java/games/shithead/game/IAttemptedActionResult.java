package games.shithead.game;

public interface IAttemptedActionResult {

    boolean isSuccessful();

    String getFailureReason();

}
