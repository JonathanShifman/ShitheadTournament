package games.shithead.moves;

import java.util.List;

import games.shithead.deck.ICard;

public interface IMove {

	EMoveType getMoveType();
	
	List<ICard> getMoveCards();
	
}
