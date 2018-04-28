package games.shithead.game.validation;

import games.shithead.game.interfaces.IGameCard;
import games.shithead.game.interfaces.IPlayerState;

import java.util.List;

public abstract class ActionValidationBase {

    /**
     * Represents the identity of the validator (whether the validation is done by the GameState or by
     * a player).
     * It's important because players are not allowed to know the faces od their hidden table cards,
     */
    protected enum ValidatorIdentity {
        GAME,
        PLAYER
    }

    /**
     * Validates the attempted taking of the pile. Only legal if the pile is not empty.
     * @param pile The contents of the pile
     * @return An ActionValidationResult corresponding to the result of the validation
     */
    private static ActionValidationResult validateTaking(List<IGameCard> pile) {
        return pile.isEmpty() ? ActionValidationResult.FOUL : ActionValidationResult.TAKE;
    }

    /**
     * Validates an attempted action by a player (whose turn it is to play).
     * @param playerState The state of the player attempting the action
     * @param cardsToPlay The cards the player is attempting to play
     * @param pile The contents of the pile
     * @return An ActionValidationResult corresponding to the result of the validation
     */
    protected static ActionValidationResult validateAction(IPlayerState playerState, List<IGameCard> cardsToPlay, List<IGameCard> pile, ValidatorIdentity validatorIdentity){
        if(cardsToPlay.isEmpty()) {
            return validateTaking(pile);
        }
        if(!ActionValidationUtils.cardsAreAvailableForPlay(playerState, cardsToPlay)) {
            return ActionValidationResult.FOUL;
        }
        if(validatorIdentity == ValidatorIdentity.PLAYER && ActionValidationUtils.cardsAreContained(playerState.getHiddenTableCards(), cardsToPlay)) {
            return ActionValidationResult.UNKNOWN;
        }
        if(!ActionValidationUtils.allCardsHaveTheSameRank(cardsToPlay)) {
            return ActionValidationResult.FOUL;
        }
        int playedRank = cardsToPlay.get(0).getCardFace().get().getRank();
        if(ActionValidationUtils.rankIsAlwaysAccepted(playedRank)) {
            return ActionValidationResult.PROCEED;
        }

        int effectiveTopCardRank = 0;
        for(IGameCard gameCard : pile) {
            int currentCardRank = gameCard.getCardFace().get().getRank();
            if(currentCardRank == 3) {
                continue;
            }
            effectiveTopCardRank = currentCardRank == 2 ? 0 : currentCardRank;
            break;
        }
        if(effectiveTopCardRank == 7 && playedRank <= effectiveTopCardRank ||
                effectiveTopCardRank != 7 && playedRank >= effectiveTopCardRank) {
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
        int interruptRank = cardsToInterrupt.get(0).getCardFace().get().getRank();
        int count = 0;
        for(IGameCard gameCard : pile) {
            if(gameCard.getCardFace().get().getRank() == interruptRank) {
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
