package games.shithead.messages;

/**
 * Sent from PlayerActor to GameActor.
 * Tells the game actor to register the player.
 */
public class RegisterPlayerMessage {

    // The name of the player asking to be registered
    private final String playerName;

    public RegisterPlayerMessage(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }
}
