package games.shithead.messages;

/**
 * Sent from <code>GameActor</code> to <code>PlayerActor</code>.
 * Sends a player their allocated id.
 *
 */
public class PlayerIdMessage {

    // The id that has been allocated for the player
    private final int playerId;

    public PlayerIdMessage(int id) {
        this.playerId = id;
    }

	public int getPlayerId() {
		return playerId;
	}
    
}
