package games.shithead.game.interfaces;

import java.util.Optional;

import games.shithead.deck.ICardFace;

/**
 * This interface represents a game card.
 * Allows to classify a card by nullifying the cardFace field, in case the player receiving the message
 * isn't allowed to know the value of the card.
 */
public interface IGameCard {

	/**
	 * @return The card face of this game card
	 */
	Optional<ICardFace> getCardFace();

	/**
	 * @return The unique id of this game card
	 */
	int getUniqueId();

	/**
	 * Generates a classified clone of the game card.
	 * Creates a new IGameCard instance with the same unique id and a nullified card face
	 * @return The classified clone
	 */
    IGameCard classifiedClone();

	/**
	 * Generates a revealed clone of the game card.
	 * Creates a new IGameCard instance with the same unique id and card face
	 * @return The revealed clone
	 */
	IGameCard revealedClone();
}
