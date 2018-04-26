package games.shithead.deck;

/**
 * This interface represents a card face, i.e. the front side of a card which shows its rank and suit.
 * A card's suit is effectively irrelevant in this game, but is still implemented for convenience.
 */
public interface ICardFace {

	/**
	 * @return The rank of the card face
	 */
	int getRank();

	/**
	 * @return The suit of the card face
	 */
	int getSuit();

}
