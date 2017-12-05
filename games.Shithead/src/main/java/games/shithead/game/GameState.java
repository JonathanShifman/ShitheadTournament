package games.shithead.game;

import akka.actor.ActorRef;
import games.shithead.deck.Card;
import games.shithead.deck.ICard;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class GameState {

    private Map<Integer, PlayerInfo> players;

    public GameState(int targetPlayerId, Map<Integer, PlayerInfo> players) {
        this.players = new HashMap<>();

        players.forEach((id, info)->{
            this.players.put(id, classifyInfo(info, id, targetPlayerId));
        });

    }

    private PlayerInfo classifyInfo(PlayerInfo info, int idOfCardsOwner, int idOfPlayerToSendTo) {
        ActorRef playerRef = (idOfPlayerToSendTo==idOfCardsOwner) ? info.getPlayerRef() : null;
        PlayerInfo classifiedInfo = new PlayerInfo(playerRef);

        classifiedInfo.getRevealedTableCards().addAll(info.getRevealedTableCards());

        classifiedInfo.getHiddenTableCards().addAll(info.getHiddenTableCards()
            .stream()
            .map(this::classifyCard)
            .collect(Collectors.toList()));
        classifiedInfo.getHandCards().addAll(info.getHandCards()
                .stream()
                .map(card -> (idOfCardsOwner==idOfPlayerToSendTo) ? card : classifyCard(card))
                .collect(Collectors.toList()));
        return classifiedInfo;
    }

    private ICard classifyCard(ICard card) {
        return new Card(-1, -1, card.getUniqueId());
    }

    public Map<Integer, PlayerInfo> getPlayers() {
        return players;
    }
}
