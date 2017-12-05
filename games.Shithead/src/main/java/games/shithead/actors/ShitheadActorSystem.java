package games.shithead.actors;

import akka.actor.ActorSystem;

public enum ShitheadActorSystem{

    INSTANCE;

    public static final String GAME_ACTOR_PATH = "/user/GameActor";
    private ActorSystem actorSystem = ActorSystem.create("ShitheadActorSystem");


    ActorSystem getActorSystem(){
        return actorSystem;
    }
}
