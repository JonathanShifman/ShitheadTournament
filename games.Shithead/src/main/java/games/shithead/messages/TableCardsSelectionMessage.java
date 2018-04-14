package games.shithead.messages;

import java.util.List;

/**
 * Sent from <code>PlayerActor</code> to <code>GameActor</code>.
 * Informs the game manager which 3 cards the player decided to put on the table.
 *
 */
public class TableCardsSelectionMessage {

	// The ids of the revealed table cards chosen by the player
	private final List<Integer> selectedCardsIds;
	
	public TableCardsSelectionMessage(List<Integer> selectedCardsIds) {
		this.selectedCardsIds = selectedCardsIds;
	}
	
	public List<Integer> getSelectedCardsIds() {
		return selectedCardsIds;
	}
}
