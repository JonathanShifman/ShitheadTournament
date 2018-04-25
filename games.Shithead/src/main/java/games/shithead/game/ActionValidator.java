package games.shithead.game;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ActionValidator {

    private static ActionValidationResult validateTaking(List<IGameCard> pile) {
        return pile.isEmpty() ? ActionValidationResult.TAKE : ActionValidationResult.FOUL;
    }

    public static ActionValidationResult validateAction(IPlayerHand playerHand, List<IGameCard> cardsToPlay, List<IGameCard> pile){
        if(cardsToPlay.isEmpty()) {
            return validateTaking(pile);
        }
        if(!cardsAreAvailableForPlay(playerHand, cardsToPlay)) {
            return ActionValidationResult.FOUL;
        }
        if(!allCardsHaveTheSameValue(cardsToPlay)) {
            return ActionValidationResult.FOUL;
        }
        int playedValue = cardsToPlay.get(0).getCardFace().get().getRank();
        if(valueIsAlwaysValid(playedValue)) {
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
            return unsuccessfulAttemptIsAllowed(playerHand, cardsToPlay) ?
                    ActionValidationResult.TAKE :
                    ActionValidationResult.FOUL;
        }
    }

    private static boolean unsuccessfulAttemptIsAllowed(IPlayerHand playerHand, List<IGameCard> cardsToPlay) {
        return cardsAreContained(playerHand.getRevealedTableCards(), cardsToPlay) ||
                cardsAreContained(playerHand.getHiddenTableCards(), cardsToPlay);
    }

    private static boolean cardsAreAvailableForPlay(IPlayerHand playerHand, List<IGameCard> cardsToPlay) {
        if(playerHand.getHandCards().isEmpty() && playerHand.getRevealedTableCards().isEmpty()) {
            return cardsToPlay.size() == 1 && cardsAreContained(playerHand.getHiddenTableCards(), cardsToPlay);
        }
        if(playerHand.getHandCards().isEmpty()) {
            return cardsAreContained(playerHand.getRevealedTableCards(), cardsToPlay);
        }
        return cardsAreContained(playerHand.getHandCards(), cardsToPlay) ||
                (cardsAreContained(cardsToPlay, playerHand.getHandCards()) &&
                cardsAreContained(Stream.concat(playerHand.getHandCards().stream(), playerHand.getRevealedTableCards().stream())
                .collect(Collectors.toList()), cardsToPlay));
    }

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

    private static boolean valueIsAlwaysValid(int playedValue) {
        return Arrays.asList(new int[] {2, 3, 10, 15}).contains(playedValue);
    }

    /**
     * Makes sure the entire list contains cards of the same value.
     *
     * @param cardsToPlay The cards the player attempts to play/interrupt with
     * @return
     */
    private static boolean allCardsHaveTheSameValue(List<IGameCard> cardsToPlay) {
        return cardsToPlay.stream()
                .map(gameCard -> gameCard.getCardFace().get().getRank())
                .distinct()
                .count() == 1;
    }

    public static ActionValidationResult validateInterruption(IPlayerHand playerHand, List<IGameCard> cardsToInterrupt, List<IGameCard> pile){
        if(cardsToInterrupt.isEmpty() || !allCardsHaveTheSameValue(cardsToInterrupt)) {
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
