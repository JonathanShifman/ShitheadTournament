package games.shithead.deck;

public class CardFace implements ICardFace {

	private final int rank;
	private final int suit;
	
	public CardFace(int rank, int suit) {
		this.rank = rank;
		this.suit = suit;
	}

	/**
	 * Copy constructor
	 * @param cardFaceToCopyFrom The card face to copy from
	 */
	public CardFace(ICardFace cardFaceToCopyFrom) {
		this.rank = cardFaceToCopyFrom.getRank();
		this.suit = cardFaceToCopyFrom.getSuit();
	}

	@Override
	public int getRank() {
		return rank;
	}

	@Override
	public int getSuit() {
		return suit;
	}

}
