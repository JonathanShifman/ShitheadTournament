package games.shithead.game;

import akka.actor.ActorRef;

import java.util.List;

public interface IPlayerInfo {
	
	List<IGameCard> getHandCards();
	
	List<IGameCard> getRevealedTableCards();
	
	List<IGameCard> getHiddenTableCards();

	List<IGameCard> getPendingSelectionCards();

}
