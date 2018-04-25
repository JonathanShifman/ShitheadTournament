package games.shithead.game;

import akka.actor.ActorRef;

import java.util.List;
import java.util.Map;

public interface IPlayerHand {
	
	List<IGameCard> getHandCards();
	
	List<IGameCard> getRevealedTableCards();
	
	List<IGameCard> getHiddenTableCards();

	List<IGameCard> getPendingSelectionCards();

	int getNumOfCardsRemaining();

	void removeAll(List<IGameCard> gameCards);

	Map<String, List<IGameCard>> getCardListsMap();

    IPlayerHand publicClone();

	IPlayerHand privateClone();
}
