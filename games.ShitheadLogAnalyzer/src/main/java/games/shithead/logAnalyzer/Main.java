package games.shithead.logAnalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import com.google.common.collect.Lists;
import org.w3c.dom.*;


public class Main {

    public static void main(String[] args) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.newDocument();
            Element gameElement = dom.createElement("game");
            dom.appendChild(gameElement);
            // FIXME
            File logFile = new File("C:\\dev\\shithead-tournament\\games.ShitheadLogAnalyzer\\src\\main\\resources\\game.log");
            BufferedReader reader = new BufferedReader(new FileReader(logFile));
            String line;
            NextExpectedEvent nextExpectedEvent = NextExpectedEvent.GAME_START;
            int numberOfPlayers = 0;
            Element currentMoveElement = dom.createElement("move");
            currentMoveElement.setAttribute("move-id", "0");
            Element currentGameStateElement = null;
            while ((line = reader.readLine()) != null) {
                if(nextExpectedEvent == NextExpectedEvent.GAME_START && line.contains("Adding player with player id")) {
                    numberOfPlayers++;
                }
                if(nextExpectedEvent == NextExpectedEvent.GAME_START && line.contains("Starting game") && !line.contains("cycle")) {
                    gameElement.setAttribute("num-of-players", ((Integer)numberOfPlayers).toString());
                    nextExpectedEvent = NextExpectedEvent.CYCLE_START;
                }
                if(nextExpectedEvent == NextExpectedEvent.CYCLE_START && line.contains("Starting game cycle")) {
                    nextExpectedEvent = NextExpectedEvent.GAME_STATE;
                }
                if(nextExpectedEvent == NextExpectedEvent.MOVE_ID && line.contains("Next move id: ")) {
                    gameElement.appendChild(currentMoveElement);
                    String moveId = line.split("Next move id: ")[1];
                    currentMoveElement = dom.createElement("move");
                    currentMoveElement.setAttribute("move-id", moveId);
                    nextExpectedEvent = NextExpectedEvent.PERFORMING_ACTION;
                }
                if(nextExpectedEvent == NextExpectedEvent.PLAYER_STATE) {
                    if (line.contains("Player") && line.contains("state")) {
                        Element playerStateElement = getPlayerStateElement(dom, line);
                        currentGameStateElement.appendChild(playerStateElement);
                    }
                    else {
                        nextExpectedEvent = NextExpectedEvent.PILE;
                    }
                }
                if(nextExpectedEvent == NextExpectedEvent.PILE && line.contains("Pile:")) {
                    currentGameStateElement.appendChild(getPileElement(dom, line));
                    currentMoveElement.appendChild(currentGameStateElement);
                    nextExpectedEvent = NextExpectedEvent.MOVE_ID;
                }
                if(nextExpectedEvent == NextExpectedEvent.GAME_STATE && line.contains("Current Game State")) {
                    currentGameStateElement = dom.createElement("game-state");
                    nextExpectedEvent = NextExpectedEvent.PLAYER_STATE;
                }
                if(nextExpectedEvent == NextExpectedEvent.PERFORMING_ACTION && line.contains("Performing action by player")) {
                    nextExpectedEvent = NextExpectedEvent.GAME_STATE;
                }
            }

            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            tr.transform(new DOMSource(dom),
                    new StreamResult(new File("C:\\dev\\shithead-tournament\\games.ShitheadLogAnalyzer\\src\\main\\resources\\game.xml")));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Element getPlayerStateElement(Document dom, String line) {
        Element playerStateElement = dom.createElement("player-state");
        String playerId = extractPlayerId(line);
        playerStateElement.setAttribute("player-id", playerId);
        List<String> cardListNames = Arrays.asList(new String[] {"hand", "table-visible", "table-hidden", "pending-selection"});
        Iterator<String> cardListNamesIterator = cardListNames.iterator();
        int lastParenthesisIndex = -1;
        for(int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if(c == '[') {
                lastParenthesisIndex = i;
            }
            else if(c ==']') {
                String cardListString = line.substring(lastParenthesisIndex + 1, i);
                Element cardListElement = getCardListElement(Arrays.asList(cardListString.split(", ")), dom);
                cardListElement.setAttribute("card-list-name", cardListNamesIterator.next());
                playerStateElement.appendChild(cardListElement);
            }
        }
        return playerStateElement;
    }

    private static String extractPlayerId(String line) {
        String splitPart = line.split("Player ")[1];
        return splitPart.substring(0, splitPart.indexOf(' '));
    }

    private static Element getPileElement(Document dom, String line) {
        String cardListString = line.substring(line.indexOf('[') + 1, line.indexOf(']'));
        Element cardListElement = getCardListElement(Arrays.asList(cardListString.split(", ")), dom);
        cardListElement.setAttribute("card-list-name", "pile");
        return cardListElement;
    }

    private static Element getCardListElement(List<String> cardList, Document dom) {
        Element cardListElement = dom.createElement("cardList");
        cardList.stream()
                .filter(cardSymbol -> !cardSymbol.isEmpty())
                .map(cardSymbol -> getCardElement(cardSymbol, dom))
                .forEach(cardElement -> cardListElement.appendChild(cardElement));
        return cardListElement;
    }

    private static Element getCardElement(String cardSymbol, Document dom) {
        Element cardElement = dom.createElement("card");
        cardElement.setAttribute("rank", cardSymbol);
        return cardElement;
    }


}
