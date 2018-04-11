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
	
	private final int playerId;
	private final int nextPlayerTurn;
	private final int nextMoveId;
	
	public AcceptedActionMessage(int playerId, int nextPlayerTurn, int nextMoveId) {
		this.playerId = playerId;
		this.nextPlayerTurn = nextPlayerTurn;
		this.nextMoveId = nextMoveId;
	}

	public int getPlayerId() {
		return playerId;
	}

	public int getNextPlayerTurn() {
		return nextPlayerTurn;
	}

	public int getNextMoveId() {
		return nextMoveId;
	}
}
