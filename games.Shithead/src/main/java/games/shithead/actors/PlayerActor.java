package games.shithead.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import games.shithead.game.GameState;
import games.shithead.game.IGameCard;
import games.shithead.game.PlayerTurnInfo;
import games.shithead.messages.*;

import java.util.List;

public abstract class PlayerActor extends AbstractActor {

    private String playerName;
    private int playerId = -1;

    public PlayerActor(String name){
        this.playerName = name;
        ActorSelection gameActor = ShitheadActorSystem.INSTANCE.getActorSystem()
                .actorSelection(ShitheadActorSystem.getActorUrl(ShitheadActorSystem.GAME_MASTER_ACTOR_NAME));

        //upon creation all players ask the game actor to allocate player id so it's unique
        gameActor.tell(new AllocateIdRequest(), self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                //after getting back the allocated id, we can register the player
                .match(IdMessage.class, this::receiveId)
                .match(GameState.class, this::analyzeGameState)
                .match(HandSelectionRequest.class, this::chooseCardsForHand)
                .match(NotifyPlayersTurnMessage.class, this::makeMove)
                .matchAny(this::unhandled)
                .build();
    }

    private void chooseCardsForHand(HandSelectionRequest request) {
        List<IGameCard> cardsForChoosing = request.getCardsForChoosing();
        HandSelectionReply selection = makeStartCardsSelection(cardsForChoosing);
        sender().tell(selection, self());
    }

    protected abstract HandSelectionReply makeStartCardsSelection(List<IGameCard> cardsForChoosing);

    private void receiveId(IdMessage idMessage) {
    	this.playerId = idMessage.playerId;
        sender().tell(new RegisterPlayerMessage(playerId, playerName), self());
    }

    private void makeMove(NotifyPlayersTurnMessage notification){
        if(playerId != notification.playerToNotify){
            System.out.println("Got request to make a move with wrong id");
            return;
        }
        sender().tell(getPlayerMove(), self());
    }

    // these two methods are to be implemented by the players
    protected abstract PlayerTurnInfo getPlayerMove();

    public abstract void analyzeGameState(GameState gameState);
}
