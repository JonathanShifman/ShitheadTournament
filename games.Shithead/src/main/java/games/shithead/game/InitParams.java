package games.shithead.game;

import java.util.List;
import java.util.Map;

public class InitParams {

    private boolean withDeckCards = false;

    private boolean withIdsToNames = false;

    private boolean withPlayingQueue = false;

    private List<String> deckCardDescriptions = null;

    private Map<Integer, String> idsToNames = null;

    private List<Integer> playingQueue = null;

    public InitParams withDeckCards(List<String> deckCardDescriptions) {
        this.withDeckCards = true;
        this.deckCardDescriptions = deckCardDescriptions;
        return this;
    }

    public InitParams withIdsToNames(Map<Integer, String> idsToNames) {
        this.withIdsToNames = true;
        this.idsToNames = idsToNames;
        return this;
    }

    public InitParams withPlayingQueue(List<Integer> playingQueue) {
        this.withPlayingQueue = true;
        this.playingQueue = playingQueue;
        return this;
    }

    public boolean isWithDeckCards() {
        return withDeckCards;
    }

    public boolean isWithIdsToNames() {
        return withIdsToNames;
    }

    public boolean isWithPlayingQueue() {
        return withPlayingQueue;
    }

    public List<String> getDeckCardDescriptions() {
        return deckCardDescriptions;
    }

    public Map<Integer, String> getIdsToNames() {
        return idsToNames;
    }

    public List<Integer> getPlayingQueue() {
        return playingQueue;
    }
}
