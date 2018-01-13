package games.shithead.messages;

/**
 * Sent from <code>PlayerActor</code> to <code>GameActor</code>.
 * Tells the game manager to register the player.
 *
 */
public class RegisterPlayerMessage {

    public int playerId;
    public String playerName;

    public RegisterPlayerMessage(int playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
    }
}
