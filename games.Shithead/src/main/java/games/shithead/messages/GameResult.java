package games.shithead.messages;

public class GameResult {

    // The id of the losing player
    private final int losingPlayerId;

    // The name of the losing player
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
