package games.shithead.actors;

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
		for(IGameCard handCard : handCards) {
			List<IGameCard> cardsToPlay = new LinkedList<>();
			cardsToPlay.add(handCard);
			if(ActionValidator.canPlay(cardsToPlay, pile)) {
				List<Integer> cardsToPut = new LinkedList<>();
				cardsToPut.add(handCard.getUniqueId());
				return new PlayerActionInfo(playerId.get(), cardsToPut, false);
			}
		}
		return new PlayerActionInfo(playerId.get(), new LinkedList<>(), false);
	}

	@Override
	protected void considerInterruption() {
		// Nah
	}

}
