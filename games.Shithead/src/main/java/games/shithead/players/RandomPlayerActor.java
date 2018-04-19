package games.shithead.players;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import games.shithead.game.ActionValidator;
import games.shithead.game.IGameCard;
import games.shithead.messages.PlayerActionMessage;

/**
 * Implementation of the Random Player
 */
public class RandomPlayerActor extends PlayerActor {

	@Override
	public String getName() {
		return "Random Player";
	}

	/**
	 * Random player strategy:
	 * Choose table cards at random. (Surprising, huh?)
	 */
	@Override
	protected List<Integer> chooseRevealedTableCards(List<IGameCard> cards, int numOfRevealedTableCardsToChoose) {
		int remainingNumberOfCardsToChoose = numOfRevealedTableCardsToChoose;
		List<Integer> chosenRevealedTableCardIds = new ArrayList<Integer>();
		for(IGameCard card : cards) {
			if(remainingNumberOfCardsToChoose > 0) {
				chosenRevealedTableCardIds.add(card.getUniqueId());
				remainingNumberOfCardsToChoose--;
			}
		}
		return chosenRevealedTableCardIds;
	}

	/**
	 * Random player strategy:
	 * Put a single playable card at random, or take the pile if there are none.
	 */
	@Override
	protected PlayerActionMessage getPlayerMove() {
		List<Integer> cardsToPut = new LinkedList<>();
		int cardId;
		if(handCards.isEmpty()) {
			cardId = getFirstPlayableCardId(revealedTableCards);
			cardsToPut.add(cardId);
		}
		else {
			cardId = getFirstPlayableCardId(handCards);
			cardsToPut.add(cardId);
		}
		return new PlayerActionMessage(cardsToPut, currentMoveId);
	}

	private int getFirstPlayableCardId(List<IGameCard> cards) {
		for(IGameCard card : cards) {
			List<IGameCard> cardsToPlay = new LinkedList<>();
			cardsToPlay.add(card);
			if(ActionValidator.canPlay(cardsToPlay, pile)) {
				return card.getUniqueId();
			}
		}
		return -1;
	}

	/**
	 * Random player strategy:
	 * Random player makes no interruptions.
	 */
	@Override
	protected List<IGameCard> getInterruptionCards() {
		// Nah
		return null;
	}

}
