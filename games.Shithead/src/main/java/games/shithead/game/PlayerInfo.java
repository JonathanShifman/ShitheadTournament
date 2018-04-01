package games.shithead.game;

import akka.actor.ActorRef;

import java.util.ArrayList;
import java.util.List;

public class PlayerInfo implements IPlayerInfo {

	private final ActorRef playerRef;
	private List<Integer> handCardIds;
	private List<Integer> revealedTableCardIds;
	private List<Integer> hiddenTableCardIds;
	private List<Integer> pendingSelectionCardIds;
	
	public PlayerInfo(ActorRef playerRef) {
		this.playerRef = playerRef;
		this.handCardIds = new ArrayList<>();
		this.revealedTableCardIds = new ArrayList<>();
		this.hiddenTableCardIds = new ArrayList<>();
		this.pendingSelectionCardIds = new ArrayList<>();
	}

	@Override
	public ActorRef getPlayerRef() {
		return playerRef;
	}

	@Override
	public List<Integer> getHandCardIds() {
		return handCardIds;
	}

	@Override
	public List<Integer> getRevealedTableCardIds() {
		return revealedTableCardIds;
	}

	@Override
	public List<Integer> getHiddenTableCardIds() {
		return hiddenTableCardIds;
	}

	@Override
	public List<Integer> getPendingSelectionCardIds() {
		return pendingSelectionCardIds;
	}
}
