package games.shithead.game;

import akka.actor.ActorRef;

import java.util.List;

public interface IPlayerInfo {
	
	ActorRef getPlayerRef();
	
	List<Integer> getHandCardIds();
	
	List<Integer> getRevealedTableCardIds();
	
	List<Integer> getHiddenTableCardIds();

	List<Integer> getPendingSelectionCardIds();

}
