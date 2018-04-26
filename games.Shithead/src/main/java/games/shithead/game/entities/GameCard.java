package games.shithead.game.entities;

import java.util.Optional;

import games.shithead.deck.ICardFace;
import games.shithead.game.interfaces.IGameCard;

public class GameCard implements IGameCard {

	private final Optional<ICardFace> cardFace;
	private final int uniqueId;
	
	public GameCard(ICardFace cardFace, int uniqueId) {
		this.cardFace = Optional.ofNullable(cardFace);
		this.uniqueId = uniqueId;
	}

	@Override
	public Optional<ICardFace> getCardFace() {
		return cardFace;
	}

	@Override
	public int getUniqueId() {
		return uniqueId;
	}

	@Override
	public IGameCard classifiedClone() {
		return new GameCard(null, uniqueId);
	}

	@Override
	public IGameCard revealedClone() {
		return new GameCard(cardFace.get(), uniqueId);
	}

}
