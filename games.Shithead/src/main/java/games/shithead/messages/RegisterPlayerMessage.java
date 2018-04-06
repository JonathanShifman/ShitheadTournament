package games.shithead.messages;

/**
 * Sent from <code>PlayerActor</code> to <code>GameActor</code>.
 * Tells the game manager to register the player.
 *
 */
public class RegisterPlayerMessage {

    private String playerName;

    public RegisterPlayerMessage(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }
}
