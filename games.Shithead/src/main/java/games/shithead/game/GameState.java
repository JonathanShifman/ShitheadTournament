package games.shithead.game;

import akka.actor.ActorRef;
import games.shithead.deck.CardFace;
import games.shithead.deck.ICardFace;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class GameState {

    private Map<Integer, PlayerInfo> players;
    private ICardFace currentTopCard;

    public GameState(int targetPlayerId, Map<Integer, PlayerInfo> players, ICardFace currentTopCard) {
        this.players = new HashMap<>();

        players.forEach((id, info)->{
            //this.players.put(id, classifyInfo(info, id, targetPlayerId));
        });

        this.currentTopCard = new CardFace(currentTopCard);

    }

//    private PlayerInfo classifyInfo(PlayerInfo info, int idOfCardsOwner, int idOfPlayerToSendTo) {
//        ActorRef playerRef = (idOfPlayerToSendTo==idOfCardsOwner) ? info.getPlayerRef() : null;
//        PlayerInfo classifiedInfo = new PlayerInfo(playerRef);
//
//        classifiedInfo.getRevealedTableCardIds().addAll(info.getRevealedTableCardIds());
//        classifiedInfo.getHiddenTableCardIds().addAll(info.getHiddenTableCardIds()
//            .stream()
//            .map(this::classifyCard)
//            .collect(Collectors.toList()));
//        classifiedInfo.getHandCardIds().addAll(info.getHandCardIds()
//                .stream()
//                .map(card -> (idOfCardsOwner==idOfPlayerToSendTo) ? card : classifyCard(card))
//                .collect(Collectors.toList()));
//        return classifiedInfo;
//    }

    private IGameCard classifyCard(IGameCard card) {
        return new GameCard(null, card.getUniqueId());
    }

    public Map<Integer, PlayerInfo> getPlayers() {
        return players;
    }

    public ICardFace getCurrentTopCard() {
        return currentTopCard;
    }
}
