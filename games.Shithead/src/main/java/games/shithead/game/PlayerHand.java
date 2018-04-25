package games.shithead.game;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerHand implements IPlayerHand {

	private List<IGameCard> handCards;
	private List<IGameCard> revealedTableCards;
	private List<IGameCard> hiddenTableCards;
	private List<IGameCard> pendingSelectionCards;
	private Map<String, List<IGameCard>> cardListsMap;

	public PlayerHand() {
		this.handCards = new ArrayList<>();
		this.revealedTableCards = new ArrayList<>();
		this.hiddenTableCards = new ArrayList<>();
		this.pendingSelectionCards = new ArrayList<>();

		createCardListsMap();
	}

	public PlayerHand(List<IGameCard> handCards, List<IGameCard> revealedTableCards,
					  List<IGameCard> hiddenTableCards, List<IGameCard> pendingSelectionCards) {
		this.handCards = handCards;
		this.revealedTableCards = revealedTableCards;
		this.hiddenTableCards = hiddenTableCards;
		this.pendingSelectionCards = pendingSelectionCards;

		createCardListsMap();
	}

	private void createCardListsMap() {
		this.cardListsMap = new LinkedHashMap<>();
		cardListsMap.put("Hand", handCards);
		cardListsMap.put("Table Revealed", revealedTableCards);
		cardListsMap.put("Table Hidden", hiddenTableCards);
		cardListsMap.put("Pending Selection", pendingSelectionCards);
	}

	@Override
	public List<IGameCard> getHandCards() {
		return handCards;
	}

	@Override
	public List<IGameCard> getRevealedTableCards() {
		return revealedTableCards;
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
		return handCards.size() + revealedTableCards.size() + hiddenTableCards.size() + pendingSelectionCards.size();
	}

	@Override
	public void removeAll(List<IGameCard> gameCards) {
		// FIXME: Comparing by reference?
		this.handCards.removeAll(gameCards);
		this.revealedTableCards.removeAll(gameCards);
		this.hiddenTableCards.removeAll(gameCards);
		this.pendingSelectionCards.removeAll(gameCards);
	}

	@Override
	public Map<String, List<IGameCard>> getCardListsMap() {
		return cardListsMap;
	}

	@Override
	public IPlayerHand publicClone() {
		return new PlayerHand(
			listClassifiedClone(handCards),
			listRevealedClone(revealedTableCards),
			listClassifiedClone(hiddenTableCards),
			listClassifiedClone(pendingSelectionCards)
		);
	}

	@Override
	public IPlayerHand privateClone() {
		return new PlayerHand(
				listRevealedClone(handCards),
				listRevealedClone(revealedTableCards),
				listClassifiedClone(hiddenTableCards),
				listRevealedClone(pendingSelectionCards)
		);
	}

	private List<IGameCard> listClassifiedClone(List<IGameCard> listToClone) {
		return listToClone.stream()
				.map(gameCard -> gameCard.classifiedClone())
				.collect(Collectors.toList());
	}

	private List<IGameCard> listRevealedClone(List<IGameCard> listToClone) {
		return listToClone.stream()
				.map(gameCard -> gameCard.revealedClone())
				.collect(Collectors.toList());
	}
}
