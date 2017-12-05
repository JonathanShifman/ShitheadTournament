package games.shithead.game;

import akka.actor.ActorRef;
import games.shithead.deck.ICard;

import java.util.ArrayList;
import java.util.List;

public class PlayerInfo implements IPlayerInfo {

	private final ActorRef playerRef;
	private List<ICard> handCards;
	private List<ICard> revealedTableCards;
	private List<ICard> hiddenTableCards;
	
	public PlayerInfo(ActorRef playerRef) {
		this.playerRef = playerRef;
		this.handCards = new ArrayList<>();
		this.revealedTableCards = new ArrayList<>();
		this.hiddenTableCards = new ArrayList<>();
	}

	@Override
	public ActorRef getPlayerRef() {
		return playerRef;
	}

	@Override
	public List<ICard> getHandCards() {
		return handCards;
	}

	@Override
	public List<ICard> getRevealedTableCards() {
		return revealedTableCards;
	}

	@Override
	public List<ICard> getHiddenTableCards() {
		return hiddenTableCards;
	}
	
}
