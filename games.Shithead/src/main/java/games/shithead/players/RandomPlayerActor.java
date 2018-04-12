package games.shithead.players;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import games.shithead.game.ActionValidator;
import games.shithead.game.IGameCard;
import games.shithead.messages.PlayerActionInfo;

public class RandomPlayerActor extends PlayerActor {

	@Override
	public String getName() {
		return "Random Player";
	}

	@Override
	protected List<Integer> chooseRevealedTableCards(List<IGameCard> cards) {
		int remainingNumberOfCardsToChoose = 0;
		List<Integer> chosenRevealedTableCardIds = new ArrayList<Integer>();
		for(IGameCard card : cards) {
			if(remainingNumberOfCardsToChoose > 0) {
				this.revealedTableCards.add(card);
				chosenRevealedTableCardIds.add(card.getUniqueId());
				remainingNumberOfCardsToChoose--;
			}
			else {
				this.handCards.add(card);
			}
		}
		return chosenRevealedTableCardIds;
	}

	@Override
	protected PlayerActionInfo getPlayerMove() {
		for(IGameCard handCard : handCards) {
			List<IGameCard> cardsToPlay = new LinkedList<>();
			cardsToPlay.add(handCard);
			if(ActionValidator.canPlay(cardsToPlay, pile)) {
				List<Integer> cardsToPut = new LinkedList<>();
				cardsToPut.add(handCard.getUniqueId());
				return new PlayerActionInfo(cardsToPut, nextMoveId);
			}
		}
		return new PlayerActionInfo(new LinkedList<>(), nextMoveId);
	}

	@Override
	protected void considerInterruption() {
		// Nah
	}

}
