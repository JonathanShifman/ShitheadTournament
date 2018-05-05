package games.shithead.messages;

import games.shithead.game.InitParams;

public class InitParamsMessage {

    private final InitParams initParams;

    public InitParamsMessage(InitParams initParams) {
        this.initParams = initParams;
    }

    public InitParams getInitParams() {
        return initParams;
    }
}
