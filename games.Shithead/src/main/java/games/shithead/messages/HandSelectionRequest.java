package games.shithead.messages;

import games.shithead.game.IGameCard;

import java.util.List;

public class HandSelectionRequest {
    private List<IGameCard> cardsForChoosing;
    public HandSelectionRequest(List<IGameCard> cardsForChoosing) {
        this.cardsForChoosing = cardsForChoosing;
    }

    public List<IGameCard> getCardsForChoosing() {
        return cardsForChoosing;
    }

    public void setCardsForChoosing(List<IGameCard> cardsForChoosing) {
        this.cardsForChoosing = cardsForChoosing;
    }
}
