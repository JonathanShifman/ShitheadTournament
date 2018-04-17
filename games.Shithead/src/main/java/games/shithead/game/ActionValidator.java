package games.shithead.game;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ActionValidator {

    public static boolean canTake(List<IGameCard> pile) {
        return !pile.isEmpty();
    }

    public static boolean canPlay(IGameCard cardToPlay, List<IGameCard> pile){
        List<IGameCard> cardsToPlay = new LinkedList<>();
        cardsToPlay.add(cardToPlay);
        return canPlay(cardsToPlay, pile);
    }

    public static boolean canPlay(List<IGameCard> cardsToPlay, List<IGameCard> pile){
        if(cardsToPlay.isEmpty() || !allCardsHaveTheSameValue(cardsToPlay)) {
            return false;
        }
        int playedValue = cardsToPlay.get(0).getCardFace().get().getRank();
        if(valueIsAlwaysValid(playedValue)) {
            return true;
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
        return effectiveTopCardValue == 7 ?
                playedValue <= effectiveTopCardValue :
                playedValue >= effectiveTopCardValue;
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

    public static boolean canInterrupt(List<IGameCard> cardsToInterrupt, List<IGameCard> pile){
        if(cardsToInterrupt.isEmpty() || !allCardsHaveTheSameValue(cardsToInterrupt)) {
            return false;
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
        return cardsToInterrupt.size() + count == 4;
    }
}
