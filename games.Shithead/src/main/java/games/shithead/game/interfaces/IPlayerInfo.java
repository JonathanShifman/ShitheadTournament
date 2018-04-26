package games.shithead.game.interfaces;

import akka.actor.ActorRef;

/**
 * Connects between a player's id, name and actor ref.
 * Used by the game actor to handle communication with the players and logging.
 */
public interface IPlayerInfo {

    /**
     * @return The player's id
     */
    int getPlayerId();

    /**
     * @return The player's name
     */
    String getPlayerName();

    /**
     * @return The player's actor ref
     */
    ActorRef getPlayerRef();

}
