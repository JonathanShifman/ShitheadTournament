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

    // The number of players in the game
    private final int numOfPlayers;

    public ChooseVisibleTableCardsMessage(List<IGameCard> cardsPendingSelection, int numOfVisibleTableCardsToBeChosen, int numOfPlayers) {
        this.cardsPendingSelection = cardsPendingSelection;
        this.numOfVisibleTableCardsToBeChosen = numOfVisibleTableCardsToBeChosen;
        this.numOfPlayers = numOfPlayers;
    }

    public List<IGameCard> getCardsPendingSelection() {
        return cardsPendingSelection;
    }

    public int getNumOfVisibleTableCardsToBeChosen() {
        return numOfVisibleTableCardsToBeChosen;
    }

    public int getNumOfPlayers() {
        return numOfPlayers;
    }
}
