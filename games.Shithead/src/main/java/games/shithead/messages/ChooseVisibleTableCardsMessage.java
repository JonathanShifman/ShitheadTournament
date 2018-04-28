package games.shithead.messages;

import java.util.List;

import games.shithead.game.interfaces.IGameCard;

/**
 * Sent from GameActor to PlayerActor.
 * Asks a player to make a selection of visible table cards. The non-selected cards will
 * become the player's hand cards.
 */
public class ChooseVisibleTableCardsMessage {

    // The list of cards the player can choose from
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
