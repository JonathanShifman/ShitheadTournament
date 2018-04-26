package games.shithead.deck;

import java.util.HashMap;
import java.util.Map;

public class CardDescriptionGenerator {

    private static Map<Integer, String> rankValuesToRepresentations = new HashMap<Integer, String>() {{
        for (int i = 2; i <= 10; i++) {
            put(i, Integer.toString(i));
        }
        put(11, "J");
        put(12, "Q");
        put(13, "K");
        put(14, "A");
        put(15, "R");
    }};

    private static Map<Integer, String> suitValuesToRepresentations = new HashMap<Integer, String>() {{
        put(1, "S");
        put(2, "H");
        put(3, "C");
        put(4, "D");
    }};

    public static String cardFaceToDescription(ICardFace cardFace) {
        return cardRankToRepresentation(cardFace.getRank()) + cardSuitToRepresentation(cardFace.getSuit());
    }

    public static String cardRankToRepresentation(int rankValue) {
        return rankValuesToRepresentations.get(rankValue);
    }

    public static String cardSuitToRepresentation(int suitValue) {
        return suitValuesToRepresentations.get(suitValue);
    }

}
