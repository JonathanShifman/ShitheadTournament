package games.shithead.messages;

import java.util.List;

/**
 * Sent from <code>PlayerActor</code> to <code>GameActor</code>.
 * Informs the game manager which 3 cards the player decided to put on the table.
 *
 */
public class TableCardsSelectionMessage {
	
	private List<Integer> selectedCardsIds;
	private int playerId;
	
	public TableCardsSelectionMessage(List<Integer> selectedCardsIds, int playerId) {
		this.selectedCardsIds = selectedCardsIds;
		this.playerId = playerId;
	}
	
	public List<Integer> getSelectedCardsIds() {
		return selectedCardsIds;
	}

	public int getPlayerId() {
		return playerId;
	}
}
