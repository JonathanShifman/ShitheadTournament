package games.shithead.messages;

public class GameResult {

    private final int losingPlayerId;
    private final String losingPlayerName;

    public GameResult(int losingPlayerId, String losingPlayerName) {
        this.losingPlayerId = losingPlayerId;
        this.losingPlayerName = losingPlayerName;
    }

    public int getLosingPlayerId() {
        return losingPlayerId;
    }

    public String getLosingPlayerName() {
        return losingPlayerName;
    }
}
