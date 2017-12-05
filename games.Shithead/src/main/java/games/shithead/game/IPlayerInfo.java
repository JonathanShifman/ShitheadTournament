package games.shithead.game;

import java.util.List;

import games.shithead.deck.ICard;
import games.shithead.players.IShitheadPlayer;

public interface IPlayerInfo {
	
	IShitheadPlayer getPlayer();
	
	List<ICard> getHandCards();
	
	List<ICard> getRevealedTableCards();
	
	List<ICard> getHiddenTableCards();

}
