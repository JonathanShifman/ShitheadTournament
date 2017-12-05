package games.shithead.moves;

import java.util.List;

import games.shithead.deck.ICard;

public class TakeMove implements IMove {

	@Override
	public EMoveType getMoveType() {
		return EMoveType.TAKE;
	}

	@Override
	public List<ICard> getMoveCards() {
		return null;
	}

}
