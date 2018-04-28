package games.shithead.players;

import games.shithead.game.interfaces.IPlayerState;
import games.shithead.game.validation.ActionValidationResult;
import games.shithead.game.interfaces.IGameCard;
import games.shithead.game.entities.PlayerActionInfo;
import games.shithead.game.validation.ActionValidatorForPlayer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the Simple Player
 */
public class SimplePlayer extends PlayerActor {

    /**
     * This class is used to compare two cards by their ranks.
     * The comparison works as follows:
     * Any card with a special rank is considered more valuable than a regular card.
     * The order between regular cards is defined based on their numeric rank value.
     * The order between special cards is defined manually within the GameCardRankComparator class.
     */
    private class GameCardRankComparator implements Comparator<IGameCard> {

        private List<Integer> specialRanksOrdering = Arrays.asList(new Integer[] {2, 3, 10, 15});

        @Override
        public int compare(IGameCard o1, IGameCard o2) {
            int diff = specialRanksOrdering.indexOf(o1.getCardFace().get().getRank()) -
                    specialRanksOrdering.indexOf(o2.getCardFace().get().getRank());
            return diff != 0 ? diff : o1.getCardFace().get().getRank() - o2.getCardFace().get().getRank();
        }
    }

    /**
     * This class is used to compare two entries of the players map according the
     * the number of cards the players have remaining.
     */
    private class VictimComparator implements Comparator<Map.Entry<Integer, IPlayerState>> {

        @Override
        public int compare(Map.Entry<Integer, IPlayerState> o1, Map.Entry<Integer, IPlayerState> o2) {
            return Integer.compare(o1.getValue().getNumOfCardsRemaining(), o2.getValue().getNumOfCardsRemaining());
        }
    }

    @Override
    public String getName() {
        return "Simple Player";
    }

    /**
     * Simple player strategy:
     * Choose the strongest cards (as determined by the comparator) to be the visible table cards.
     */
    @Override
    protected List<Integer> chooseVisibleTableCards(List<IGameCard> cardsToChooseFrom, int numOfVisibleTableCardsToChoose) {
        cardsToChooseFrom.sort(new GameCardRankComparator().reversed());
        List<Integer> chosenVisibleTableCardIds = new ArrayList<Integer>();
        Iterator<IGameCard> iterator = cardsToChooseFrom.iterator();
        while (numOfVisibleTableCardsToChoose-- > 0) {
            IGameCard currentCard = iterator.next();
            chosenVisibleTableCardIds.add(currentCard.getUniqueId());
        }
        return chosenVisibleTableCardIds;
    }

    /**
     * Simple player strategy:
     * Find the weakest playable rank, and play all available cards of that rank.
     * When playing a joker, choose the victim to be the player who holds the least cards.
     */
    @Override
    protected PlayerActionInfo getPlayerMove() {
        if(handCards.isEmpty() && visibleTableCards.isEmpty()) {
            // If only hidden table cards are remaining, play one at random.
            List<Integer> cardsToPutIds = new LinkedList<>();
            cardsToPutIds.add(hiddenTableCards.get(0).getUniqueId());
            return new PlayerActionInfo(cardsToPutIds);
        }
        int chosenRank;
        List<Integer> cardsToPlayIds;
        if(handCards.isEmpty()) {
            // Choose cards from visible table cards.
            chosenRank = getWeakestRankWithBestResult(visibleTableCards);
            cardsToPlayIds = getAllCardIdsWithRank(chosenRank, visibleTableCards);
        }
        else {
            /* Chose cards from hand cards. If all hand cards are played at once, add visible table cards
             * with the same rank */
            chosenRank = getWeakestRankWithBestResult(handCards);
            cardsToPlayIds = getAllCardIdsWithRank(chosenRank, handCards);
            if(cardsToPlayIds.size() == handCards.size()) {
                cardsToPlayIds.addAll(getAllCardIdsWithRank(chosenRank, visibleTableCards));
            }
        }

        if(chosenRank == 15) {
            // Choose a victim if a joker was played
            return new PlayerActionInfo(cardsToPlayIds, chooseVictimId());
        }
        return new PlayerActionInfo(cardsToPlayIds);
    }

    /**
     * Simple player strategy:
     * Attempt interruption whenever possible.
     */
    @Override
    protected PlayerActionInfo getPlayerInterruption() {
        if(pile.isEmpty()) {
            return null;
        }

        // Calculate the rank on top of the pile, and the amount of remaining cards needed for interruption
        int pileTopRank = pile.get(0).getCardFace().get().getRank();
        int consecutiveTopRankCards = 0;
        Iterator<IGameCard> iterator = pile.iterator();
        IGameCard currentCard;
        while (iterator.hasNext()) {
            currentCard = iterator.next();
            if(currentCard.getCardFace().get().getRank() == pileTopRank) {
                consecutiveTopRankCards++;
            }
            else {
                break;
            }
        }
        int numOfCardsNeededForInterruption = 4 - consecutiveTopRankCards;

        // Get interruption cards from hand cards, and if possible from visible table cards
        List<Integer> cardsToInterruptIds = getAllCardIdsWithRank(pileTopRank, handCards);
        if(cardsToInterruptIds.size() == handCards.size()) {
            cardsToInterruptIds.addAll(getAllCardIdsWithRank(pileTopRank, visibleTableCards));
        }
        if(cardsToInterruptIds.size() >= numOfCardsNeededForInterruption) {
            return new PlayerActionInfo(cardsToInterruptIds);
        }
        return null;
    }

    /**
     * Return the weakest rank (out of the given cards) that will yield the best result (PROCEED is considered
     * better than TAKE).
     * @param cards The cards to choose from
     * @return The weakest rank, or -1 if no card is valid to play.
     */
    private int getWeakestRankWithBestResult(List<IGameCard> cards) {
        cards.sort(new GameCardRankComparator());
        int weakestRankWithProceedResult = -1;
        int weakestRankWithTakeResult = -1;
        for(IGameCard gameCard : cards) {
            List<IGameCard> cardsToValidate = new LinkedList<>();
            cardsToValidate.add(gameCard);
            ActionValidationResult validationResult = ActionValidatorForPlayer.validateAction(playerStates.get(playerId), cardsToValidate, pile);
            if(validationResult == ActionValidationResult.PROCEED && weakestRankWithProceedResult < 0) {
                weakestRankWithProceedResult = gameCard.getCardFace().get().getRank();
            }
            else if(validationResult == ActionValidationResult.TAKE && weakestRankWithTakeResult < 0) {
                weakestRankWithTakeResult = gameCard.getCardFace().get().getRank();
            }
        }
        if(weakestRankWithProceedResult > 0) {
            return weakestRankWithProceedResult;
        }
        return weakestRankWithTakeResult;
    }

    /**
     * Returns the ids of all the cards in the given lists that have the specified rank.
     * @param rank The rank for comparison
     * @param sources The lists of cards to look for cards in
     * @return A list of card ids with the specified rank
     */
    private List<Integer> getAllCardIdsWithRank(int rank, List<IGameCard>... sources) {
        List<Integer> cardIdsWithRank = new LinkedList<>();
        for(List<IGameCard> source : sources) {
            cardIdsWithRank.addAll(source.stream()
                    .filter(gameCard -> gameCard.getCardFace().get().getRank() == rank)
                    .map(gameCard -> gameCard.getUniqueId())
                    .collect(Collectors.toList()));
        }
        return cardIdsWithRank;
    }

    /**
     * Chooses the id of the player to give the cards to after playing a joker.
     * @return The id of the chosen victim.
     */
    private int chooseVictimId() {
        return playerStates.entrySet().stream()
                .filter(entry -> entry.getKey() != playerId)
                .sorted(new VictimComparator())
                .findFirst().get().getKey();
    }
}
