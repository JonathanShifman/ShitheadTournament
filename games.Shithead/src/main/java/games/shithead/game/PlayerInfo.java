package games.shithead.game;

import akka.actor.ActorRef;
import games.shithead.deck.ICardFace;

import java.util.ArrayList;
import java.util.List;

public class PlayerInfo implements IPlayerInfo {

	private final ActorRef playerRef;
	private List<IGameCard> handCards;
	private List<IGameCard> revealedTableCards;
	private List<IGameCard> hiddenTableCards;
	
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
	
}
