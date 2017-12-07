package games.shithead.game;

import akka.actor.ActorRef;
import games.shithead.deck.ICardFace;

import java.util.List;

public interface IPlayerInfo {
	
	ActorRef getPlayerRef();
	
	List<IGameCard> getHandCards();
	
	List<IGameCard> getRevealedTableCards();
	
	List<IGameCard> getHiddenTableCards();

}
