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
  hiddenTableCardRanksBackup;
  visibleTableCardRanks;
  handCardRanks;

  constructor() { }

  ngOnInit() {
      this.name = "Player Name"
      this.playerId = this.player.getAttribute('player-id');
      this.cardLists = this.player.children;

      this.hiddenTableCardRanks = [];
      this.hiddenTableCardRanksBackup = [];
      for(let card of Array.from(this.cardLists[2].children)) {
        // this.hiddenTableCardRanks.push(card.getAttribute("rank") + card.getAttribute("suit"))
        // this.hiddenTableCardRanksBackup.push(card.getAttribute("rank") + card.getAttribute("suit"))
      }

      this.visibleTableCardRanks = [];
      for(let card of Array.from(this.cardLists[1].children)) {
        // this.visibleTableCardRanks.push(card.getAttribute("rank") + card.getAttribute("suit"))
      }

      this.handCardRanks = [];
      for(let card of Array.from(this.cardLists[0].children)) {
        // this.handCardRanks.push(card.getAttribute("rank") + card.getAttribute("suit"))
      }
  }
}
