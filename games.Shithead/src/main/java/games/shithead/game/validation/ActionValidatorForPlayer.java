package games.shithead.game.validation;

import games.shithead.game.interfaces.IGameCard;
import games.shithead.game.interfaces.IPlayerState;
import games.shithead.game.logging.LoggingUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * This class is used by the players to validate potential actions
 */
public class ActionValidatorForPlayer {

    private static Logger logger = LogManager.getLogger("Player");

    /**
     * Validates the attempted taking of the pile. Only legal if the pile is not empty.
     * @param pile The contents of the pile
     * @return An ActionValidationResult corresponding to the result of the validation
     */
    private static ActionValidationResult validateTaking(List<IGameCard> pile) {
        return pile.isEmpty() ? ActionValidationResult.TAKE : ActionValidationResult.FOUL;
    }

    /**
     * Validates an attempted action by a player (whose turn it is to play).
     * @param playerState The state of the player attempting the action
     * @param cardsToPlay The cards the player is attempting to play
     * @param pile The contents of the pile
     * @return An ActionValidationResult corresponding to the result of the validation
     */
    public static ActionValidationResult validateAction(IPlayerState playerState, List<IGameCard> cardsToPlay, List<IGameCard> pile){
        if(cardsToPlay.isEmpty()) {
            return validateTaking(pile);
        }
        if(!ActionValidationUtils.cardsAreAvailableForPlay(playerState, cardsToPlay)) {
            return ActionValidationResult.FOUL;
        }
        if(ActionValidationUtils.cardsAreContained(playerState.getHiddenTableCards(), cardsToPlay)) {
            return ActionValidationResult.UNKNOWN;
        }
        if(!ActionValidationUtils.allCardsHaveTheSameRank(cardsToPlay)) {
            return ActionValidationResult.FOUL;
        }
        int playedRank = cardsToPlay.get(0).getCardFace().get().getRank();
        if(ActionValidationUtils.valueIsAlwaysAccepted(playedRank)) {
            return ActionValidationResult.PROCEED;
        }

        int effectiveTopCardValue = 0;
        for(IGameCard gameCard : pile) {
            int currentCardValue = gameCard.getCardFace().get().getRank();
            if(currentCardValue == 3) {
                continue;
            }
            effectiveTopCardValue = currentCardValue == 2 ? 0 : currentCardValue;
            break;
        }
        if(effectiveTopCardValue == 7 && playedRank <= effectiveTopCardValue ||
                effectiveTopCardValue != 7 && playedRank >= effectiveTopCardValue) {
            return ActionValidationResult.PROCEED;
        }
        else {
            return ActionValidationUtils.unacceptedAttemptIsAllowed(playerState, cardsToPlay) ?
                    ActionValidationResult.TAKE :
                    ActionValidationResult.FOUL;
        }
    }

    /**
     * Validates an attempted interruption by a player (while it's not his turn to play).
     * @param playerState The state of the player attempting the action
     * @param cardsToInterrupt The cards the player is attempting to interrupt with
     * @param pile The contents of the pile
     * @return An ActionValidationResult corresponding to the result of the validation
     */
    public static ActionValidationResult validateInterruption(IPlayerState playerState, List<IGameCard> cardsToInterrupt, List<IGameCard> pile){
        if(cardsToInterrupt.isEmpty() || !ActionValidationUtils.allCardsHaveTheSameRank(cardsToInterrupt)) {
            return ActionValidationResult.FOUL;
        }
        int interruptValue = cardsToInterrupt.get(0).getCardFace().get().getRank();
        int count = 0;
        for(IGameCard gameCard : pile) {
            if(gameCard.getCardFace().get().getRank() == interruptValue) {
                count++;
            }
            else {
                break;
            }
        }
        return cardsToInterrupt.size() + count >= 4 ?
                ActionValidationResult.PROCEED :
                ActionValidationResult.FOUL;
    }
}
