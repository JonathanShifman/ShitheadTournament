package games.shithead.moves;

import java.util.LinkedList;
import java.util.List;

import games.shithead.deck.ICard;

public class PlayMove implements IMove {
	
	private List<ICard> moveCards;
	
	public PlayMove() {
		moveCards = new LinkedList<>();
	}
	
	public PlayMove(List<ICard> moveCards) {
		this.moveCards = moveCards;
	}
	
	public void addCard(ICard card) {
		moveCards.add(card);
	}

	@Override
	public EMoveType getMoveType() {
		return EMoveType.PLAY;
	}

	@Override
	public List<ICard> getMoveCards() {
		return moveCards;
	}

}
