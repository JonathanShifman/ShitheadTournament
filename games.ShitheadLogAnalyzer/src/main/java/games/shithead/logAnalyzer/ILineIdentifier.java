package games.shithead.logAnalyzer;

public interface ILineIdentifier {

    /**
     * Attempts to match the given line to this identifier.
     * @param line The line to match
     * @return true if the line matches, false otherwise
     */
    boolean match(String line);

    /**
     * Extracts the relevant information from the line. This could be a number, a string, a collection
     * or any other type of object - depending on the specific identifier.
     * Assumes the line matches the identifier.
     * @param line The line to extract information from
     * @return The extracted info
     */
    Object extractInfo(String line);

}
