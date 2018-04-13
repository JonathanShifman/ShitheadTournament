package games.shithead.players;

import games.shithead.game.ActionValidator;
import games.shithead.game.IGameCard;
import games.shithead.messages.PlayerActionInfo;

import java.util.*;
import java.util.stream.Collectors;

public class SimplePlayerActor extends PlayerActor {

    class GameCardValueComparator implements Comparator<IGameCard> {

        private List<Integer> specialValuesOrdering = Arrays.asList(new Integer[] {2, 3, 10, 15});

        @Override
        public int compare(IGameCard o1, IGameCard o2) {
            int diff = specialValuesOrdering.indexOf(o1.getCardFace().get().getValue()) -
                    specialValuesOrdering.indexOf(o2.getCardFace().get().getValue());
            return diff != 0 ? diff : o1.getCardFace().get().getValue() - o2.getCardFace().get().getValue();
        }
    }

    @Override
    public String getName() {
        return "Simple Player";
    }

    @Override
    protected List<Integer> chooseRevealedTableCards(List<IGameCard> cards, int numOfRevealedTableCardsToChoose) {
        cards.sort(new GameCardValueComparator());
        int remainingNumberOfCardsToChoose = numOfRevealedTableCardsToChoose;
        List<Integer> chosenRevealedTableCardIds = new ArrayList<Integer>();
        for(IGameCard card : cards) {
            if(remainingNumberOfCardsToChoose > 0) {
                this.revealedTableCards.add(card);
                chosenRevealedTableCardIds.add(card.getUniqueId());
                remainingNumberOfCardsToChoose--;
            }
            else {
                this.handCards.add(card);
            }
        }
        return chosenRevealedTableCardIds;
    }

    @Override
    protected PlayerActionInfo getPlayerMove() {
        if(handCards.isEmpty()) {
            List<IGameCard> cardsToPut = getWeakestPlayableSet(revealedTableCards);
            List<Integer> cardsToPutIds = cardsToPut.stream()
                    .map(card -> card.getUniqueId())
                    .collect(Collectors.toList());
            return new PlayerActionInfo(cardsToPutIds, nextMoveId);
        }
        List<IGameCard> cardsToPut = getWeakestPlayableSet(handCards);
        if(!cardsToPut.isEmpty()) {
            int selectedValue = cardsToPut.get(0).getCardFace().get().getValue();
            for(IGameCard gameCard : revealedTableCards) {
                if (gameCard.getCardFace().get().getValue() == selectedValue) {
                    cardsToPut.add(gameCard);
                }
            }
        }
        List<Integer> cardsToPutIds = cardsToPut.stream()
                .map(card -> card.getUniqueId())
                .collect(Collectors.toList());
        return new PlayerActionInfo(cardsToPutIds, nextMoveId);
    }

    private List<IGameCard> getWeakestPlayableSet(List<IGameCard> cards) {
        cards.sort(new GameCardValueComparator());
        List<IGameCard> weakestPlayableSet = new LinkedList<>();
        int chosenValue = -1;
        for(IGameCard gameCard : cards) {
            if(chosenValue > 0) {
                if(gameCard.getCardFace().get().getValue() == chosenValue) {
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
                    chosenValue = gameCard.getCardFace().get().getValue();
                }
            }
        }
        return weakestPlayableSet;
    }

    @Override
    protected void considerInterruption() {
        // FIXME: Change later
    }
}
