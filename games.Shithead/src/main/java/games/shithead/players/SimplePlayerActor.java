package games.shithead.players;

import games.shithead.game.ActionValidator;
import games.shithead.game.IGameCard;
import games.shithead.messages.PlayerActionInfo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

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
    protected List<Integer> chooseRevealedTableCards(List<IGameCard> cards) {
        return new LinkedList<>();
    }

    @Override
    protected PlayerActionInfo getPlayerMove() {
        handCards.sort(new GameCardValueComparator());
        List<Integer> cardsToPut = new LinkedList<>();
        int chosenValue = -1;
        for(IGameCard gameCard : handCards) {
            if(chosenValue > 0) {
                if(gameCard.getCardFace().get().getValue() == chosenValue) {
                    cardsToPut.add(gameCard.getUniqueId());
                    continue;
                }
                else {
                    break;
                }
            }
            else {
                if(ActionValidator.canPlay(gameCard, pile)) {
                    cardsToPut.add(gameCard.getUniqueId());
                    chosenValue = gameCard.getCardFace().get().getValue();
                }
            }
        }

        return new PlayerActionInfo(playerId, cardsToPut, false);
    }

    @Override
    protected void considerInterruption() {
        // FIXME: Change later
    }
}
