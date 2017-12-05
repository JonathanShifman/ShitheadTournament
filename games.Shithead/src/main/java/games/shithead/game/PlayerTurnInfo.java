package games.shithead.game;

import games.shithead.deck.ICard;

import java.util.List;

public class PlayerTurnInfo {

    private int playerId;
    private List<ICard> cardsToPut;
    private boolean isInterruption;

    public PlayerTurnInfo(int playerId, List<ICard> cardsToPut, boolean isInterruption) {
        this.playerId = playerId;
        this.cardsToPut = cardsToPut;
        this.isInterruption = isInterruption;
    }

    public List<ICard> getCardsToPut() {
        return cardsToPut;
    }

    public void setCardsToPut(List<ICard> cardsToPut) {
        this.cardsToPut = cardsToPut;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public boolean isInterruption() {
        return isInterruption;
    }

    public void setInterruption(boolean interruption) {
        isInterruption = interruption;
    }
}
