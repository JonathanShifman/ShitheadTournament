package games.shithead.game;

import java.util.Optional;

import games.shithead.deck.ICardFace;

/**
 * @author Jonathan
 * 
 * Implements a game card, and is used primarily to send from one actor to another. 
 * Unlike CardFace, GameCard allows to classify a card by nullifying the cardFace field.
 * Useful for allowing a player to play a card without knowing what it is (in case of
 * a hidden table card.
 *
 */
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
