package games.shithead.game;

import games.shithead.messages.PlayerActionInfo;

import java.util.List;

public class ActionValidator {

    public static boolean canTake(List<IGameCard> pile) {
        return !pile.isEmpty();
    }

    public static boolean canPlay(List<IGameCard> cardsToPlay, List<IGameCard> pile){
        if(cardsToPlay.isEmpty() || !allCardsHaveTheSameValue(cardsToPlay)) {
            return false;
        }
        int topCardValue = pile.isEmpty() ? 0 : pile.get(0).getCardFace().get().getValue();
        return cardsToPlay.get(0).getCardFace().get().getValue() >= topCardValue;
    }

    /**
     * Makes sure the entire list contains cards of the same value.
     *
     * @param cardsToPlay The cards the player attempts to play/interrupt with
     * @return
     */
    private static boolean allCardsHaveTheSameValue(List<IGameCard> cardsToPlay) {
        return cardsToPlay.stream()
                .map(gameCard -> gameCard.getCardFace().get().getValue())
                .distinct()
                .count() == 1;
    }

    public static boolean canInterrupt(List<IGameCard> cardsToInterrupt, List<IGameCard> pile){
        return false;
    }
}
