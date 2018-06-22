package games.shithead.logAnalyzer;

import games.shithead.constants.ConstantsProvider;

public class Main {

    public static void main(String[] args) {
        try {
            String resourcesDirPath = System.getenv(ConstantsProvider.SYSTEM_ENV_VAR_NAME) + "\\games.ShitheadLogAnalyzer\\src\\main\\resources\\";
            LogAnalyzer logAnalyzer = new LogAnalyzer();
            logAnalyzer.analyze(resourcesDirPath + "game.log", resourcesDirPath + "game.xml");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }



}
