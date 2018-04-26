package games.shithead.players;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import games.shithead.game.validation.ActionValidationResult;
import games.shithead.game.validation.ActionValidatorForGame;
import games.shithead.game.interfaces.IGameCard;
import games.shithead.game.entities.PlayerActionInfo;
import games.shithead.game.validation.ActionValidatorForPlayer;

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
	protected PlayerActionInfo getPlayerMove() {
		int cardId;
		if(handCards.isEmpty() && visibleTableCards.isEmpty()) {
			cardId = hiddenTableCards.get(0).getUniqueId();
		}
		else if(handCards.isEmpty()) {
			cardId = visibleTableCards.get(0).getUniqueId();
		}
		else {
			cardId = getFirstPlayableCardId(handCards);
		}
		List<Integer> cardsToPut = new LinkedList<>();
		if(cardId > -1) {
			cardsToPut.add(cardId);
		}
		return new PlayerActionInfo(cardsToPut);
	}

	private int getFirstPlayableCardId(List<IGameCard> cards) {
		for(IGameCard card : cards) {
			List<IGameCard> cardsToPlay = new LinkedList<>();
			cardsToPlay.add(card);
			if(ActionValidatorForPlayer.validateAction(playerHands.get(playerId), cardsToPlay, pile) != ActionValidationResult.FOUL) {
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
