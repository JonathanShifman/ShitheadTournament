package games.shithead.game.validation;

import games.shithead.game.interfaces.IGameCard;
import games.shithead.game.interfaces.IPlayerState;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ActionValidationUtils {

    /**
     * Checks if the given cards are available for the player to play, based on their position in the
     * player's state.
     * @param playerState The player's state
     * @param cardsToPlay The cards attempted to play
     * @return True if the cards are available, false otherwise
     */
    public static boolean cardsAreAvailableForPlay(IPlayerState playerState, List<IGameCard> cardsToPlay) {
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
    public static boolean unacceptedAttemptIsAllowed(IPlayerState playerState, List<IGameCard> cardsToPlay) {
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
    public static boolean cardsAreContained(List<IGameCard> containing, List<IGameCard> contained) {
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
     * Checks if the given rank is always accepted, regardless of the contents of the pile.
     * @param playedRank The rank to check
     * @return True if the rank is always accepted, false otherwise
     */
    public static boolean rankIsAlwaysAccepted(int playedRank) {
        // FIXME
        return playedRank == 2 ||
                playedRank == 3 ||
                playedRank == 10 ||
                playedRank == 15;
    }

    /**
     * Makes sure the entire list contains cards of the same rank.
     * @param cardsToPlay The cards the player attempts to play/interrupt with
     * @return True if the list contains cards of the same rank, false otherwise
     */
    public static boolean allCardsHaveTheSameRank(List<IGameCard> cardsToPlay) {
        return cardsToPlay.stream()
                .map(gameCard -> gameCard.getCardFace().get().getRank())
                .distinct()
                .count() == 1;
    }
}
