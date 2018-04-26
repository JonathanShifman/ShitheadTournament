package games.shithead.game.entities;

import games.shithead.game.interfaces.IGameCard;
import games.shithead.game.interfaces.IPlayerState;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerState implements IPlayerState {

	private List<IGameCard> handCards;
	private List<IGameCard> visibleTableCards;
	private List<IGameCard> hiddenTableCards;
	private List<IGameCard> pendingSelectionCards;
	private Map<String, List<IGameCard>> cardListsMap;

	public PlayerState() {
		this.handCards = new ArrayList<>();
		this.visibleTableCards = new ArrayList<>();
		this.hiddenTableCards = new ArrayList<>();
		this.pendingSelectionCards = new ArrayList<>();

		createCardListsMap();
	}

	public PlayerState(List<IGameCard> handCards, List<IGameCard> visibleTableCards,
					   List<IGameCard> hiddenTableCards, List<IGameCard> pendingSelectionCards) {
		this.handCards = handCards;
		this.visibleTableCards = visibleTableCards;
		this.hiddenTableCards = hiddenTableCards;
		this.pendingSelectionCards = pendingSelectionCards;

		createCardListsMap();
	}

	private void createCardListsMap() {
		this.cardListsMap = new LinkedHashMap<>();
		cardListsMap.put("Hand", handCards);
		cardListsMap.put("Table Visible", visibleTableCards);
		cardListsMap.put("Table Hidden", hiddenTableCards);
		cardListsMap.put("Pending Selection", pendingSelectionCards);
	}

	@Override
	public List<IGameCard> getHandCards() {
		return handCards;
	}

	@Override
	public List<IGameCard> getVisibleTableCards() {
		return visibleTableCards;
	}

	@Override
	public List<IGameCard> getHiddenTableCards() {
		return hiddenTableCards;
	}

	@Override
	public List<IGameCard> getPendingSelectionCards() {
		return pendingSelectionCards;
	}

	@Override
	public int getNumOfCardsRemaining() {
		return handCards.size() + visibleTableCards.size() + hiddenTableCards.size() + pendingSelectionCards.size();
	}

	@Override
	public void removeAll(List<IGameCard> gameCards) {
		// FIXME: Comparing by reference?
		this.handCards.removeAll(gameCards);
		this.visibleTableCards.removeAll(gameCards);
		this.hiddenTableCards.removeAll(gameCards);
		this.pendingSelectionCards.removeAll(gameCards);
	}

	@Override
	public Map<String, List<IGameCard>> getCardListsMap() {
		return cardListsMap;
	}

	@Override
	public IPlayerState publicClone() {
		return new PlayerState(
			listClassifiedClone(handCards),
			listRevealedClone(visibleTableCards),
			listClassifiedClone(hiddenTableCards),
			listClassifiedClone(pendingSelectionCards)
		);
	}

	@Override
	public IPlayerState privateClone() {
		return new PlayerState(
				listRevealedClone(handCards),
				listRevealedClone(visibleTableCards),
				listClassifiedClone(hiddenTableCards),
				listRevealedClone(pendingSelectionCards)
		);
	}

	/**
	 * Generated a list containing classified clones (the CardFace is nullified) of the cards in the original list
	 * @param listToClone The list to clone
	 * @return The cloned list
	 */
	private List<IGameCard> listClassifiedClone(List<IGameCard> listToClone) {
		return listToClone.stream()
				.map(gameCard -> gameCard.classifiedClone())
				.collect(Collectors.toList());
	}

	/**
	 * Generated a list containing clones (the CardFace is left as it is) of the cards in the original list
	 * @param listToClone The list to clone
	 * @return The cloned list
	 */
	private List<IGameCard> listRevealedClone(List<IGameCard> listToClone) {
		return listToClone.stream()
				.map(gameCard -> gameCard.revealedClone())
				.collect(Collectors.toList());
	}
}
