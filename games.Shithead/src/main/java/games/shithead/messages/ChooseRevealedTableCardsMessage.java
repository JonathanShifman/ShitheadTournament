package games.shithead.messages;

import java.util.List;

import games.shithead.game.IGameCard;

/**
 * Sent from <code>GameActor</code> to <code>PlayerActor</code>.
 * Sends a player the 9 cards he has been dealt. 3 of them (the hidden table cards) have nullified card faces.
 *
 */
public class ChooseRevealedTableCardsMessage {

    // The number of revealed table cards that should be chosen
    private final int revealedCardsToBeChosen;

    public ChooseRevealedTableCardsMessage(int revealedCardsToBeChosen) {
        this.revealedCardsToBeChosen = revealedCardsToBeChosen;
    }

    public int getRevealedCardsToBeChosen() {
        return revealedCardsToBeChosen;
    }
}
