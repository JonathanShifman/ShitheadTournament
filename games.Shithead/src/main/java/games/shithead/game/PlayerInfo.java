package games.shithead.game;

import akka.actor.ActorRef;

public class PlayerInfo implements IPlayerInfo {

    private int playerId;
    private String playerName;
    private ActorRef playerRef;

    public PlayerInfo(int playerId, String playerName, ActorRef playerRef) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.playerRef = playerRef;
    }

    @Override
    public int getPlayerId() {
        return playerId;
    }

    @Override
    public String getPlayerName() {
        return playerName;
    }

    @Override
    public ActorRef getPlayerRef() {
        return playerRef;
    }
}
