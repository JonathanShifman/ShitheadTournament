package games.shithead.messages;

import java.util.List;

/**
 * Sent from PlayerActor to GameActor.
 * Informs the game actor which cards the player decided to put on the table.
 */
public class TableCardsSelectionMessage {

	// The ids of the visible table cards chosen by the player
	private final List<Integer> selectedCardsIds;
	
	public TableCardsSelectionMessage(List<Integer> selectedCardsIds) {
		this.selectedCardsIds = selectedCardsIds;
	}
	
	public List<Integer> getSelectedCardsIds() {
		return selectedCardsIds;
	}
}
