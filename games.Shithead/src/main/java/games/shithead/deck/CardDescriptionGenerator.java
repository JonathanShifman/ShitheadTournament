package games.shithead.deck;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.HashMap;
import java.util.Map;

public class CardDescriptionGenerator {

    private static BiMap<Integer, String> rankValuesToRepresentations = HashBiMap.create();

    private static BiMap<Integer, String> suitValuesToRepresentations = HashBiMap.create();

    static {
        for (int i = 2; i <= 10; i++) {
            rankValuesToRepresentations.put(i, Integer.toString(i));
        }
        rankValuesToRepresentations.put(11, "J");
        rankValuesToRepresentations.put(12, "Q");
        rankValuesToRepresentations.put(13, "K");
        rankValuesToRepresentations.put(14, "A");
        rankValuesToRepresentations.put(15, "R");

        suitValuesToRepresentations.put(1, "S");
        suitValuesToRepresentations.put(2, "H");
        suitValuesToRepresentations.put(3, "C");
        suitValuesToRepresentations.put(4, "D");
    }

    public static String cardFaceToDescription(ICardFace cardFace) {
        if(cardFace == null) {
            return "?";
        }
        return cardRankToRepresentation(cardFace.getRank()) + cardSuitToRepresentation(cardFace.getSuit());
    }

    public static String cardFaceToMinimalDescription(ICardFace cardFace) {
        if(cardFace == null) {
            return "?";
        }
        return cardRankToRepresentation(cardFace.getRank());
    }

    public static String cardRankToRepresentation(int rankValue) {
        return rankValuesToRepresentations.get(rankValue);
    }

    public static String cardSuitToRepresentation(int suitValue) {
        return suitValuesToRepresentations.get(suitValue);
    }

    public static CardFace descriptionToCardFace(String description) {
        int rankValue = rankValuesToRepresentations.inverse().get(description.substring(0, description.length() - 1));
        int suitValue = suitValuesToRepresentations.inverse().get(description.substring(description.length() - 1, description.length()));
        return new CardFace(rankValue, suitValue);
    }

}
