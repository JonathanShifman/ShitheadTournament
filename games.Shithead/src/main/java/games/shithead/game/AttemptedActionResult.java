package games.shithead.game;

public class AttemptedActionResult implements IAttemptedActionResult {

    private boolean successful;
    private String failureReason;

    public AttemptedActionResult() {
        this.successful = true;
    }

    public AttemptedActionResult(String failureReason) {
        this.successful = false;
        this.failureReason = failureReason;
    }

    @Override
    public boolean isSuccessful() {
        return successful;
    }

    @Override
    public String getFailureReason() {
        return failureReason;
    }
}
