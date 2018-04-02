package games.shithead.game;

import akka.actor.ActorRef;

import java.util.ArrayList;
import java.util.List;

public class PlayerInfo implements IPlayerInfo {

	private List<IGameCard> handCards;
	private List<IGameCard> revealedTableCards;
	private List<IGameCard> hiddenTableCards;
	private List<IGameCard> pendingSelectionCards;
	
	public PlayerInfo() {
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
}
