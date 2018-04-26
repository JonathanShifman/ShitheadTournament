package games.shithead.game.entities;

/**
 * Represents the card status, which is essentially its location in the game.
 */
public class CardStatus {

	// A static instance used for a card that's still in the deck
	public static final CardStatus DECK = new CardStatus(-1);

	// A static instance used for a card that's currently in the pile
	public static final CardStatus PILE = new CardStatus(-2);

	// A static instance used for a card that has already been burned
	public static final CardStatus BURNT = new CardStatus(-3);

	// The id of the player who holds a card. A value of -1 indicates the card isn't held by any player
	private int holderId;

	// The position of a card that is held by a player in the player's state
	private HeldCardPosition heldCardPosition;
	
	public CardStatus(int holderId, HeldCardPosition heldCardPosition) {
		this.holderId = holderId;
		this.heldCardPosition = heldCardPosition;
	}
	
	private CardStatus(int holderId) {
		this.holderId = holderId;
		this.heldCardPosition = null;
	}

	/**
	 * @return True if the card is held by a player, false otherwise
	 */
	public boolean isHeldByAPlayer() {
		return holderId > 0;
	}

	/**
	 * @return The id of the holder of the card
	 */
	public int getHolderId() {
		return holderId;
	}

	/**
	 * @return The position of a held card
	 */
	public HeldCardPosition getHeldCardPosition() {
		return heldCardPosition;
	}

	/**
	 * @param holderId The new holder id to set
	 */
	public void setHolderId(int holderId) {
		this.holderId = holderId;
	}

	/**
	 * @param heldCardPosition The new held card position to set
	 */
	public void setHeldCardPosition(HeldCardPosition heldCardPosition) {
		this.heldCardPosition = heldCardPosition;
	}
}
