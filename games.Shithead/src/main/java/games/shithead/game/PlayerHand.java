package games.shithead.game;

import java.util.ArrayList;
import java.util.List;

public class PlayerHand implements IPlayerHand {

	private List<IGameCard> handCards;
	private List<IGameCard> revealedTableCards;
	private List<IGameCard> hiddenTableCards;
	private List<IGameCard> pendingSelectionCards;
	
	public PlayerHand() {
		this.handCards = new ArrayList<>();
		this.revealedTableCards = new ArrayList<>();
		this.hiddenTableCards = new ArrayList<>();
		this.pendingSelectionCards = new ArrayList<>();
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
}
