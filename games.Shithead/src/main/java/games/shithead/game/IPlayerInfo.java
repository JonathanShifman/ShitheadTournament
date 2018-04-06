package games.shithead.game;

import akka.actor.ActorRef;

public interface IPlayerInfo {

    int getPlayerId();

    String getPlayerName();

    ActorRef getPlayerRef();

}
