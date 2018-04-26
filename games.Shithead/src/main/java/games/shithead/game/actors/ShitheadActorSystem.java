package games.shithead.game.actors;

import akka.actor.ActorSystem;

public enum ShitheadActorSystem{

    INSTANCE;

    public static final String GAME_ACTOR_NAME = "GameActor";
    public static final String GAME_MASTER_ACTOR_NAME = "GameMasterActor";
    private ActorSystem actorSystem = ActorSystem.create("ShitheadActorSystem");


    public ActorSystem getActorSystem(){
        return actorSystem;
    }

    public static String getActorUrl(String name){
        return "/user/" + name;
    }
}
