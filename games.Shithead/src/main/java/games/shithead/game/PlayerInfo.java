package games.shithead.game;

import java.util.List;

import games.shithead.deck.ICard;
import games.shithead.players.IShitheadPlayer;

public class PlayerInfo implements IPlayerInfo {
	
	private IShitheadPlayer player;
	private List<ICard> handCards;
	private List<ICard> revealedTableCards;
	private List<ICard> hiddenTableCards;
	
	public PlayerInfo(IShitheadPlayer player) {
		this.player = player;
	}

	@Override
	public IShitheadPlayer getPlayer() {
		return player;
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
