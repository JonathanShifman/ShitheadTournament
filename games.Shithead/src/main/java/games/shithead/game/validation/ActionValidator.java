package games.shithead.game.validation;

import games.shithead.game.interfaces.IGameCard;
import games.shithead.game.interfaces.IPlayerState;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is used to validate actions attempted by players
 */
public class ActionValidator {

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
        if(!cardsAreAvailableForPlay(playerState, cardsToPlay)) {
            return ActionValidationResult.FOUL;
        }
        if(!allCardsHaveTheSameRank(cardsToPlay)) {
            return ActionValidationResult.FOUL;
        }
        int playedValue = cardsToPlay.get(0).getCardFace().get().getRank();
        if(valueIsAlwaysAccepted(playedValue)) {
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
        if(effectiveTopCardValue == 7 && playedValue <= effectiveTopCardValue ||
                effectiveTopCardValue != 7 && playedValue >= effectiveTopCardValue) {
            return ActionValidationResult.PROCEED;
        }
        else {
            return unacceptedAttemptIsAllowed(playerState, cardsToPlay) ?
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
        if(cardsToInterrupt.isEmpty() || !allCardsHaveTheSameRank(cardsToInterrupt)) {
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

    /**
     * Checks if the given cards are available for the player to play, based on their position in the
     * player's state.
     * @param playerState The player's state
     * @param cardsToPlay The cards attempted to play
     * @return True if the cards are available, false otherwise
     */
    private static boolean cardsAreAvailableForPlay(IPlayerState playerState, List<IGameCard> cardsToPlay) {
        if(playerState.getHandCards().isEmpty() && playerState.getVisibleTableCards().isEmpty()) {
            return cardsToPlay.size() == 1 && cardsAreContained(playerState.getHiddenTableCards(), cardsToPlay);
        }
        if(playerState.getHandCards().isEmpty()) {
            return cardsAreContained(playerState.getVisibleTableCards(), cardsToPlay);
        }
        return cardsAreContained(playerState.getHandCards(), cardsToPlay) ||
                (cardsAreContained(cardsToPlay, playerState.getHandCards()) &&
                        cardsAreContained(Stream.concat(playerState.getHandCards().stream(), playerState.getVisibleTableCards().stream())
                                .collect(Collectors.toList()), cardsToPlay));
    }

    /**
     * Checks if the action is valid despite not having been accepted.
     * This can happen if the player attempted to play a hidden table card, or a visible table card
     * while holding no cards in his hand.
     * @param playerState The player's state
     * @param cardsToPlay The cards attempted to play
     * @return
     */
    private static boolean unacceptedAttemptIsAllowed(IPlayerState playerState, List<IGameCard> cardsToPlay) {
        return cardsAreContained(playerState.getVisibleTableCards(), cardsToPlay) ||
                cardsAreContained(playerState.getHiddenTableCards(), cardsToPlay);
    }

    /**
     * Checks if all cards in the second list are contained in the first list.
     * Comparison is done by the card's unique id.
     * @param containing The list that's supposed to contain the cards in the second list
     * @param contained The cards that are supposed to be contained in the first list
     * @return True if the cards are contained, false otherwise
     */
    private static boolean cardsAreContained(List<IGameCard> containing, List<IGameCard> contained) {
        List<Integer> containingIds = containing.stream()
                .map(card -> card.getUniqueId())
                .collect(Collectors.toList());
        List<Integer> notContainedIds = contained.stream()
                .map(card -> card.getUniqueId())
                .filter(cardId -> !containingIds.contains(cardId))
                .collect(Collectors.toList());
        return notContainedIds.size() == 0;
    }

    /**
     * Checks if the given value is always accepted, regardless of the contents of the pile.
     * @param playedValue The value to check
     * @return True if the value is always accepted, false otherwise
     */
    private static boolean valueIsAlwaysAccepted(int playedValue) {
        return Arrays.asList(new int[] {2, 3, 10, 15}).contains(playedValue);
    }

    /**
     * Makes sure the entire list contains cards of the same rank.
     * @param cardsToPlay The cards the player attempts to play/interrupt with
     * @return True if the list contains cards of the same rank, false otherwise
     */
    private static boolean allCardsHaveTheSameRank(List<IGameCard> cardsToPlay) {
        return cardsToPlay.stream()
                .map(gameCard -> gameCard.getCardFace().get().getRank())
                .distinct()
                .count() == 1;
    }
}
