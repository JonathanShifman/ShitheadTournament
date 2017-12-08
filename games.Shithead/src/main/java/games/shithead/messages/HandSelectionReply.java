package games.shithead.messages;

import games.shithead.game.IGameCard;

import java.util.List;

public class HandSelectionReply {

    private List<IGameCard> handCards;
    private List<IGameCard> faceupCards;

    public HandSelectionReply(List<IGameCard> handCards, List<IGameCard> faceupCards) {
        this.handCards = handCards;
        this.faceupCards = faceupCards;
    }

    public List<IGameCard> getHandCards() {
        return handCards;
    }

    public void setHandCards(List<IGameCard> handCards) {
        this.handCards = handCards;
    }

    public List<IGameCard> getFaceupCards() {
        return faceupCards;
    }

    public void setFaceupCards(List<IGameCard> faceupCards) {
        this.faceupCards = faceupCards;
    }
}
