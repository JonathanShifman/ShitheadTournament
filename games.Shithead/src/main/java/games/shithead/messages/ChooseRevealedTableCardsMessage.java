package games.shithead.messages;

import java.util.List;

import games.shithead.game.IGameCard;

/**
 * Sent from <code>GameActor</code> to <code>PlayerActor</code>.
 * Sends a player the 9 cards he has been dealt. 3 of them (the hidden table cards) have nullified card faces.
 *
 */
public class ChooseRevealedTableCardsMessage {

    private final List<IGameCard> cardsPendingSelection;

    // The number of revealed table cards that should be chosen
    private final int revealedCardsToBeChosen;

    public ChooseRevealedTableCardsMessage(List<IGameCard> cardsPendingSelection, int revealedCardsToBeChosen) {
        this.cardsPendingSelection = cardsPendingSelection;
        this.revealedCardsToBeChosen = revealedCardsToBeChosen;
    }

    public List<IGameCard> getCardsPendingSelection() {
        return cardsPendingSelection;
    }

    public int getRevealedCardsToBeChosen() {
        return revealedCardsToBeChosen;
    }
}
