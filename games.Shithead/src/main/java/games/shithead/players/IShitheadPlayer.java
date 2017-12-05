package games.shithead.players;

import java.util.List;

import games.shithead.deck.ICard;
import games.shithead.moves.IMove;

public interface IShitheadPlayer {
	
	void receiveNumberOfPlayers(int numberOfPlayers);
	
	void receivePlayerId(int playerId);
	
	void receiveRevealedTableCards(List<ICard>[] revealedTableCards);
	
	IMove makeMove();
	
	void receiveMove();

}
