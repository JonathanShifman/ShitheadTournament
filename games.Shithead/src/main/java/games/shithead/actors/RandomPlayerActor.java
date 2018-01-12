package games.shithead.actors;

import java.util.ArrayList;
import java.util.List;

import games.shithead.game.IGameCard;
import games.shithead.messages.PlayerActionInfo;

public class RandomPlayerActor extends PlayerActor {

	@Override
	public String getName() {
		return "Random Player";
	}

	@Override
	protected void chooseRevealedTableCards(List<IGameCard> cards) {
		int remainingNumberOfCardsToChoose = 3;
		for(IGameCard card : cards) {
			if(remainingNumberOfCardsToChoose > 0) {
				this.handCards.add(card);
				remainingNumberOfCardsToChoose--;
			}
			else {
				this.revealedTableCards.add(card);
			}
		}
	}

	@Override
	protected void handlePublicDeal() {
		// TODO Auto-generated method stub
	}

	@Override
	protected PlayerActionInfo getPlayerMove() {
		if(!handCards.isEmpty()) {
			//return first possible hand card
			return null;
		}
		else {
			//return first possible table card
			return null;
		}
	}

	@Override
	protected void considerInterruption() {
		// TODO Auto-generated method stub
		
	}

}
