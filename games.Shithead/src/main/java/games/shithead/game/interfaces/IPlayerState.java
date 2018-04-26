package games.shithead.game.interfaces;

import java.util.List;
import java.util.Map;

/**
 * This interface represents a player state, i.e. the cards in the player's possession
 * and their positions (in-hand, table visible, table hidden or pending selection)
 */
public interface IPlayerState {

	/**
	 * @return A list containing the player's hand cards
	 */
	List<IGameCard> getHandCards();

	/**
	 * @return A list containing the player's visible table cards
	 */
	List<IGameCard> getVisibleTableCards();

	/**
	 * @return A list containing the player's hidden table cards
	 */
	List<IGameCard> getHiddenTableCards();

	/**
	 * @return A list containing the player's pending selection cards
	 */
	List<IGameCard> getPendingSelectionCards();

	/**
	 * @return The overall number of cards the player has
	 */
	int getNumOfCardsRemaining();

	/**
	 * Removes all the specified cards from the player state.
	 * Cards that appear in the list but don't exist in the player state will be ignored.
	 * @param gameCards The cards to remove
	 */
	void removeAll(List<IGameCard> gameCards);

	/**
	 * Returns a mapping from card list names to the lists themselves.
	 * Used for logging the content of a player state.
	 * @return A map containing the player's cards lists
	 */
	Map<String, List<IGameCard>> getCardListsMap();

	/**
	 * Clones the state so that only the info that is available to other players (namely,
	 * the visible table cards) is revealed.
	 * @return The cloned state
	 */
    IPlayerState publicClone();

	/**
	 * Clones the state so that the info that is available to the player this state belongs to (namely,
	 * the hand cards, visible table cards and pending selection cards) is revealed.
	 * @return The cloned state
	 */
	IPlayerState privateClone();
}
