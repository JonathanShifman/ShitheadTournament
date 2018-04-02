package games.shithead.messages;

import java.util.List;

import games.shithead.game.IGameCard;

/**
 * Sent from <code>GameActor</code> to <code>PlayerActor</code>.
 * Informs the players which of the attempted moves was accepted (based on the order the attempts were made).
 * Also contains the id of the player who's turn it is to play next.
 *
 */
public class AcceptedActionMessage {
	
	private int playerId;
	private int nextPlayerTurn;
	
	public AcceptedActionMessage(int playerId, int nextPlayerTurn) {
		this.playerId = playerId;
		this.nextPlayerTurn = nextPlayerTurn;
	}

	public int getPlayerId() {
		return playerId;
	}

	public int getNextPlayerTurn() {
		return nextPlayerTurn;
	}
}
