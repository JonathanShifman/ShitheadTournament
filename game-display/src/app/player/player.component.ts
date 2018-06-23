import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-player',
  templateUrl: './player.component.html',
  styleUrls: ['./player.component.css']
})
export class PlayerComponent implements OnInit {
	
  @Input() player;
  playerId;
  name;
  cardLists;
  hiddenTableCardRanks;
  visibleTableCardRanks;
  handCardRanks;

  constructor() { }

  ngOnInit() {
      this.name = "Player Name"
      this.playerId = this.player.getAttribute('player-id');
      this.cardLists = this.player.children;

      this.hiddenTableCardRanks = [];
      for(let card of Array.from(this.cardLists[2].children)) {
        this.hiddenTableCardRanks.push(card.getAttribute("rank"))
      }

      this.visibleTableCardRanks = [];
      for(let card of Array.from(this.cardLists[1].children)) {
        this.visibleTableCardRanks.push(card.getAttribute("rank"))
      }

      this.handCardRanks = [];
      for(let card of Array.from(this.cardLists[0].children)) {
        this.handCardRanks.push(card.getAttribute("rank"))
      }
  }

}
