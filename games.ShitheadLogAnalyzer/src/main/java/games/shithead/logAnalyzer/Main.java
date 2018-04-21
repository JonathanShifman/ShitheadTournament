package games.shithead.logAnalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
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
            Element rootElement = dom.createElement("root");
            dom.appendChild(rootElement);
            // FIXME
            File logFile = new File("C:\\dev\\shithead-tournament\\games.ShitheadLogAnalyzer\\src\\main\\resources\\game.log");
            BufferedReader reader = new BufferedReader(new FileReader(logFile));
            String line;
            while ((line = reader.readLine()) != null) {
                if(line.contains("Current Game State")) {
                    Element moveElement = getMoveElement(line, dom);
                    rootElement.appendChild(moveElement);
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
            System.out.println(e.getMessage());
        }
    }

    private static Element getMoveElement(String line, Document dom) {
        List<String> cardListStrings = new LinkedList<String>();
        int openingParentheses = 0;
        int lastParenthesisIndex = -1;
        for(int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if(c == '[') {
                openingParentheses++;
                if(openingParentheses == 2){
                    lastParenthesisIndex = i;
                }
            }
            else if(c ==']') {
                openingParentheses--;
                if(openingParentheses == 1){
                    cardListStrings.add(line.substring(lastParenthesisIndex + 1, i));
                }
            }
        }

        Element moveElement = dom.createElement("move");
        List<List<String>> cardListStringsLists = Lists.partition(cardListStrings, 4);
        cardListStringsLists.stream()
                .map(cardListStringsList -> getPlayerElement(cardListStringsList, dom))
                .forEach(playerElement -> moveElement.appendChild(playerElement));

        return moveElement;
    }

    private static Element getPlayerElement(List<String> cardListStringsList, Document dom) {
        Element playerElement = dom.createElement("player");
        cardListStringsList.stream()
                .map(cardListString -> (List<String>)Arrays.asList(cardListString.split(", ")))
                .map(cardList -> getCardListElement(cardList, dom))
                .forEach(cardListElement -> playerElement.appendChild(cardListElement));
        return playerElement;
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
        cardElement.appendChild(dom.createTextNode(cardSymbol));
        return cardElement;
    }


}
