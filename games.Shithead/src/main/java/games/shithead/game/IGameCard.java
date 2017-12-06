package games.shithead.game;

import games.shithead.deck.ICardFace;

public interface IGameCard {
	
	ICardFace getCardFace();
	
	int getUniqueId();
	
}
