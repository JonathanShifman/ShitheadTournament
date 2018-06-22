import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
	
	title = 'app';
	numOfPlayers;
	moveElements;
	currentMoveId;
	currentMoveElement;
	currentPlayerStateElements;
	
	ngOnInit() {
		var xmlhttp = new XMLHttpRequest();
		xmlhttp.open("GET", "assets/game.xml", false);
		xmlhttp.send();
		var xmlDoc = xmlhttp.responseXML;

		var gameElement = xmlDoc.getElementsByTagName('game')[0];
		this.numOfPlayers = gameElement.getAttribute('num-of-players');
		this.moveElements = gameElement.children;
		this.currentMoveId = 0;
		this.updateCurrentStates();
	}

	updateCurrentStates() {
		this.currentMoveElement = this.moveElements[this.currentMoveId];
		var playerStateElements = this.currentMoveElement.getElementsByTagName('game-state')[0].getElementsByTagName('player-state');
		this.currentPlayerStateElements = [];
		for(let playerStateElement of Array.from(playerStateElements)) {
			this.currentPlayerStateElements.push(playerStateElement)
		}
	}
}
  