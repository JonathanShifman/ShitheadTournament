package games.shithead.game;

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

	private final ICardFace cardFace;
	private final int uniqueId;
	
	public GameCard(ICardFace cardFace, int uniqueId) {
		this.cardFace = cardFace;
		this.uniqueId = uniqueId;
	}

	@Override
	public ICardFace getCardFace() {
		return cardFace;
	}

	@Override
	public int getUniqueId() {
		return uniqueId;
	}

}