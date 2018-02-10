package games.shithead.game;

import java.util.Optional;

import games.shithead.deck.ICardFace;

public interface IGameCard {
	
	Optional<ICardFace> getCardFace();
	
	int getUniqueId();
	
}
