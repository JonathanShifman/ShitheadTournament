package games.shithead.game;

import akka.actor.ActorRef;
import games.shithead.deck.ICard;

import java.util.List;

public interface IPlayerInfo {
	
	ActorRef getPlayerRef();
	
	List<ICard> getHandCards();
	
	List<ICard> getRevealedTableCards();
	
	List<ICard> getHiddenTableCards();

}
