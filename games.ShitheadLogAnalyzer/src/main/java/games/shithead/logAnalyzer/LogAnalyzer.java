package games.shithead.logAnalyzer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class LogAnalyzer {

    private Document dom;

    public LogAnalyzer() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        dom = db.newDocument();
    }

    /**
     * Analyzes a log file containing information about a shithead game contents and outputs
     * the analysis results to an XML file.
     * @param logFilePath The path of the log file to analyze
     * @param outputFilePath The desired path of the output XML file
     * @throws IOException
     * @throws TransformerException
     */
    public void analyze(String logFilePath, String outputFilePath) throws IOException, TransformerException {
        Element gameElement = dom.createElement("game");
        dom.appendChild(gameElement);
        File logFile = new File(logFilePath);
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
                String moveId = substringsBetween(line, "Next move id: ", " ").get(0);
                currentMoveElement = dom.createElement("move");
                currentMoveElement.setAttribute("move-id", moveId);
                nextExpectedEvent = NextExpectedEvent.PERFORMING_ACTION;
            }
            if(nextExpectedEvent == NextExpectedEvent.PLAYER_STATE) {
                if (line.contains("Player") && line.contains("state")) {
                    Element playerStateElement = getPlayerStateElement(line);
                    currentGameStateElement.appendChild(playerStateElement);
                }
                else {
                    nextExpectedEvent = NextExpectedEvent.PILE;
                }
            }
            if(nextExpectedEvent == NextExpectedEvent.PILE && line.contains("Pile:")) {
                currentGameStateElement.appendChild(getCardListElement(cardListStringToCardList(substringsBetweenSquareBrackets(line).get(0)), "pile"));
                currentMoveElement.appendChild(currentGameStateElement);
                nextExpectedEvent = NextExpectedEvent.MOVE_ID;
            }
            if(nextExpectedEvent == NextExpectedEvent.GAME_STATE && line.contains("Current Game State")) {
                currentGameStateElement = dom.createElement("game-state");
                nextExpectedEvent = NextExpectedEvent.PLAYER_STATE;
            }
            if(nextExpectedEvent == NextExpectedEvent.PERFORMING_ACTION && line.contains("Performing action by player")) {
                Element actionElement = dom.createElement("action");
                String playerId = substringsBetween(line, "by player", ".").get(0);
                actionElement.setAttribute("player-id", playerId);
                String cardListString = substringsBetween(line, "[", "]").get(0);
                Element cardListElement = getCardListElement(Arrays.asList(cardListString.split(", ")));
                cardListElement.setAttribute("card-list-name", "action");
                actionElement.appendChild(cardListElement);
                currentMoveElement.appendChild(actionElement);
                nextExpectedEvent = NextExpectedEvent.GAME_STATE;
            }
        }

        createOutputFile(outputFilePath);
    }

    /**
     * Returns an XML element representing a player's state at a certain point in the game
     * @param line The log line containing the player state info
     * @return The generated player state element
     */
    private Element getPlayerStateElement(String line) {
        Element playerStateElement = dom.createElement("player-state");
        String playerId = substringsBetween(line, "Player ", " ").get(0);
        playerStateElement.setAttribute("player-id", playerId);
        List<String> cardListNames = Arrays.asList(new String[] {"hand", "table-visible", "table-hidden", "pending-selection"});
        Iterator<String> cardListNamesIterator = cardListNames.iterator();
        for(String cardListString : substringsBetweenSquareBrackets(line)) {
            Element cardListElement = getCardListElement(Arrays.asList(cardListString.split(", ")), cardListNamesIterator.next());
            playerStateElement.appendChild(cardListElement);
        }
        return playerStateElement;
    }

    /**
     * Converts a list representing a card list to the actual card list.
     * @param cardListString The string to convert
     * @return The generated list of card symbols
     */
    private List<String> cardListStringToCardList(String cardListString) {
        return Arrays.asList(cardListString.split(", "));
    }

    /**
     * Returns a named card list XML element.
     * @param cardList A list of cards to be included in the card list element
     * @param cardListName The name of the card list
     * @return The generated card list element
     */
    private Element getCardListElement(List<String> cardList, String cardListName) {
        Element cardListElement = getCardListElement(cardList);
        cardListElement.setAttribute("card-list-name", cardListName);
        return cardListElement;
    }

    /**
     * Returns an unnamed card list XML element.
     * @param cardList A list of cards to be included in the card list element
     * @return The generated card list element
     */
    private Element getCardListElement(List<String> cardList) {
        Element cardListElement = dom.createElement("cardList");
        cardList.stream()
                .filter(cardSymbol -> !cardSymbol.isEmpty())
                .map(cardSymbol -> getCardElement(cardSymbol))
                .forEach(cardElement -> cardListElement.appendChild(cardElement));
        return cardListElement;
    }

    /**
     * Generates a card XML element with the given card symbol.
     * @param cardSymbol The card symbol
     * @return The generated card element
     */
    private Element getCardElement(String cardSymbol) {
        Element cardElement = dom.createElement("card");
        cardElement.setAttribute("rank", cardSymbol);
        return cardElement;
    }

    /**
     * Invokes the substringsBetween method with square bracket delimiters
     * @param original The original string to extract substrings from
     * @return The generated list of substrings
     */
    private List<String> substringsBetweenSquareBrackets(String original) {
        return substringsBetween(original, "[", "]");
    }

    /**
     * Returns a list of substrings appearing in between two strings in the original string.
     * For example, substringBetween("ABCDAEC", "A", "C") would return the list: ["B", "E"].
     * @param original The original string to extract substrings from
     * @param left The string that delimits the desired substring on the left
     * @param right The string that delimits the desired substring on the right
     * @return The generated list of substrings
     */
    private List<String> substringsBetween(String original, String left, String right) {
        List<String> substrings = new LinkedList<>();
        int leftStartingIndex = original.indexOf(left);
        while (leftStartingIndex > 0) {
            int postLeftIndex = leftStartingIndex + left.length();
            int rightStartingIndex = original.indexOf(right, postLeftIndex);
            if(rightStartingIndex < 0) {
                rightStartingIndex = original.length();
            }
            String substring = original.substring(postLeftIndex, rightStartingIndex);
            substrings.add(substring);
            int postRightIndex = rightStartingIndex + right.length();
            leftStartingIndex = original.indexOf(left, postRightIndex);
        }
        return substrings;
    }

    /**
     * Creates the XML file containing the analyzing result.
     * @param outputFilePath The path of he file to create
     * @throws TransformerException
     */
    private void createOutputFile(String outputFilePath) throws TransformerException {
        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty(OutputKeys.METHOD, "xml");
        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        tr.transform(new DOMSource(dom),
                new StreamResult(new File(outputFilePath)));
    }

}
