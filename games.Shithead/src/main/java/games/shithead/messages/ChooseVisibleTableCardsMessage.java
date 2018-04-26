package games.shithead.messages;

import java.util.List;

import games.shithead.game.interfaces.IGameCard;

/**
 * Sent from <code>GameActor</code> to <code>PlayerActor</code>.
 * Sends a player the 9 cards he has been dealt. 3 of them (the hidden table cards) have nullified card faces.
 *
 */
public class ChooseVisibleTableCardsMessage {

    private final List<IGameCard> cardsPendingSelection;

    // The number of visible table cards that should be chosen
    private final int numOfVisibleTableCardsToBeChosen;

    public ChooseVisibleTableCardsMessage(List<IGameCard> cardsPendingSelection, int numOfVisibleTableCardsToBeChosen) {
        this.cardsPendingSelection = cardsPendingSelection;
        this.numOfVisibleTableCardsToBeChosen = numOfVisibleTableCardsToBeChosen;
    }

    public List<IGameCard> getCardsPendingSelection() {
        return cardsPendingSelection;
    }

    public int getNumOfVisibleTableCardsToBeChosen() {
        return numOfVisibleTableCardsToBeChosen;
    }
}
