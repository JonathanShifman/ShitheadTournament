package games.shithead.players;

import java.util.List;

import games.shithead.deck.ICard;

public abstract class AShitheadPlayer implements IShitheadPlayer {
	
	private int numberOfPlayers;
	private int playerId;
	
	@Override
	public void receiveNumberOfPlayers(int numberOfPlayers) {
		
	}

	@Override
	public void receivePlayerId(int playerId) {
		this.playerId = playerId;

	}

	@Override
	public void receiveRevealedTableCards(List<ICard>[] revealedTableCards) {
		// TODO Auto-generated method stub

	}

}
