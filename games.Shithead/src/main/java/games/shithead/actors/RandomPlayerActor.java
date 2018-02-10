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
	protected List<Integer> chooseRevealedTableCards(List<IGameCard> cards) {
		int remainingNumberOfCardsToChoose = 3;
		List<Integer> chosenRevealdTableCardIds = new ArrayList<Integer>();
		for(IGameCard card : cards) {
			if(remainingNumberOfCardsToChoose > 0) {
				this.revealedTableCards.add(card);
				chosenRevealdTableCardIds.add(card.getUniqueId());
				remainingNumberOfCardsToChoose--;
			}
			else {
				this.handCards.add(card);
			}
		}
		return chosenRevealdTableCardIds;
	}

	@Override
	protected void handlePublicDeal() {
		// Random Player doesn't give a shit about such subtleties 
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
		// Nah
	}

}
