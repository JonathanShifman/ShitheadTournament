package games.shithead.game.entities;

/**
 * An enum representing the possible position of a card in a player's state
 */
public enum HeldCardPosition {
	/**
	 * The card is pending the player's selection, to be either a hand card or
	 * a visible table card.
	 */
	PENDING_SELECTION,

	/**
	 * The card is in the player's hand
	 */
	IN_HAND,

	/**
	 * The card is one of the player's visible table cards
	 */
	TABLE_VISIBLE,

	/**
	 * The card is one of the player's hidden table cards
	 */
	TABLE_HIDDEN
}
