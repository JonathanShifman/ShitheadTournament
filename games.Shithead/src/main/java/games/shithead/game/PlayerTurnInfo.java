package games.shithead.game;

import games.shithead.deck.ICardFace;

import java.util.List;

public class PlayerTurnInfo {

    private int playerId;
    private List<ICardFace> cardsToPut;
    private boolean isInterruption;

    public PlayerTurnInfo(int playerId, List<ICardFace> cardsToPut, boolean isInterruption) {
        this.playerId = playerId;
        this.cardsToPut = cardsToPut;
        this.isInterruption = isInterruption;
    }

    public List<ICardFace> getCardsToPut() {
        return cardsToPut;
    }

    public void setCardsToPut(List<ICardFace> cardsToPut) {
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
