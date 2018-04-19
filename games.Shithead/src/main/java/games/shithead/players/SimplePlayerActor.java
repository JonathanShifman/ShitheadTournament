package games.shithead.players;

import games.shithead.game.ActionValidator;
import games.shithead.game.IGameCard;
import games.shithead.messages.PlayerActionMessage;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the Simple Player
 */
public class SimplePlayerActor extends PlayerActor {

    /**
     * This class is used to compare two cards by their ranks.
     * The comparison works as follows:
     * Any card with a special rank is considered more valuable than a regular card.
     * The order between regular cards is defined based on their numeric rank value.
     * The order between special cards is defined manually within the GameCardValueComparator class.
     */
    class GameCardValueComparator implements Comparator<IGameCard> {

        private List<Integer> specialRanksOrdering = Arrays.asList(new Integer[] {2, 3, 10, 15});

        @Override
        public int compare(IGameCard o1, IGameCard o2) {
            int diff = specialRanksOrdering.indexOf(o1.getCardFace().get().getRank()) -
                    specialRanksOrdering.indexOf(o2.getCardFace().get().getRank());
            return diff != 0 ? diff : o1.getCardFace().get().getRank() - o2.getCardFace().get().getRank();
        }
    }

    @Override
    public String getName() {
        return "Simple Player";
    }

    /**
     * Simple player strategy:
     * Choose the strongest cards (as determined by the comparator) to be the revealed table cards.
     */
    @Override
    protected List<Integer> chooseRevealedTableCards(List<IGameCard> cards, int numOfRevealedTableCardsToChoose) {
        cards.sort(new GameCardValueComparator());
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
     * Simple player strategy:
     * Find the weakest playable rank, and play all cards of that rank in the player's possession.
     */
    @Override
    protected PlayerActionMessage getPlayerMove() {
        if(handCards.isEmpty()) {
            List<IGameCard> cardsToPut = getWeakestPlayableSet(revealedTableCards);
            List<Integer> cardsToPutIds = cardsToPut.stream()
                    .map(card -> card.getUniqueId())
                    .collect(Collectors.toList());
            return new PlayerActionMessage(cardsToPutIds, currentMoveId);
        }
        List<IGameCard> cardsToPut = getWeakestPlayableSet(handCards);
        if(!cardsToPut.isEmpty()) {
            int selectedValue = cardsToPut.get(0).getCardFace().get().getRank();
            for(IGameCard gameCard : revealedTableCards) {
                if (gameCard.getCardFace().get().getRank() == selectedValue) {
                    cardsToPut.add(gameCard);
                }
            }
        }
        List<Integer> cardsToPutIds = cardsToPut.stream()
                .map(card -> card.getUniqueId())
                .collect(Collectors.toList());
        return new PlayerActionMessage(cardsToPutIds, currentMoveId);
    }

    private List<IGameCard> getWeakestPlayableSet(List<IGameCard> cards) {
        cards.sort(new GameCardValueComparator());
        List<IGameCard> weakestPlayableSet = new LinkedList<>();
        int chosenValue = -1;
        for(IGameCard gameCard : cards) {
            if(chosenValue > 0) {
                if(gameCard.getCardFace().get().getRank() == chosenValue) {
                    weakestPlayableSet.add(gameCard);
                    continue;
                }
                else {
                    break;
                }
            }
            else {
                if(ActionValidator.canPlay(gameCard, pile)) {
                    weakestPlayableSet.add(gameCard);
                    chosenValue = gameCard.getCardFace().get().getRank();
                }
            }
        }
        return weakestPlayableSet;
    }

    /**
     * Simple player strategy:
     * Attempt interruption whenever possible.
     */
    @Override
    protected List<IGameCard> getInterruptionCards() {
        if(pile.isEmpty()) {
            return null;
        }
        int pileTopValue = pile.get(0).getCardFace().get().getRank();
        int consecutiveTopValueCards = 0;
        Iterator<IGameCard> iterator = pile.iterator();
        IGameCard currentCard;
        while (iterator.hasNext()) {
            currentCard = iterator.next();
            if(currentCard.getCardFace().get().getRank() == pileTopValue) {
                consecutiveTopValueCards++;
            }
            else {
                break;
            }
        }

        int cardsNeededForInterruption = 4 - consecutiveTopValueCards;
        List<IGameCard> interruptionCards = new LinkedList<>();
        for(IGameCard gameCard : handCards) {
            if(gameCard.getCardFace().get().getRank() == pileTopValue) {
                interruptionCards.add(gameCard);
                cardsNeededForInterruption--;
                if(cardsNeededForInterruption == 0) {
                    return interruptionCards;
                }
            }
        }
        if(interruptionCards.size() == handCards.size()) {
            for(IGameCard gameCard : revealedTableCards) {
                if(gameCard.getCardFace().get().getRank() == pileTopValue) {
                    interruptionCards.add(gameCard);
                    cardsNeededForInterruption--;
                    if(cardsNeededForInterruption == 0) {
                        return interruptionCards;
                    }
                }
            }
        }
        return null;
    }
}
