package games.shithead.messages;

import java.util.List;

import games.shithead.game.IGameCard;

public class AcceptedActionMessage {
	
	private int playerId;
	private List<IGameCard> cards;
	private int nextPlayerTurn;
	
	public AcceptedActionMessage(int playerId, List<IGameCard> cards, int nextPlayerTurn) {
		this.playerId = playerId;
		this.cards = cards;
		this.nextPlayerTurn = nextPlayerTurn;
	}

	public int getPlayerId() {
		return playerId;
	}

	public List<IGameCard> getCards() {
		return cards;
	}

	public int getNextPlayerTurn() {
		return nextPlayerTurn;
	}

}
