import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';


import { AppComponent } from './components/app/app.component';
import { PlayerComponent } from './components/player/player.component';
import { TableComponent } from './components/table/table.component';


@NgModule({
  declarations: [
    AppComponent,
    PlayerComponent,
    TableComponent
  ],
  imports: [
    BrowserModule
  ],
  providers: [],
  bootstrap: [TableComponent]
})
export class AppModule { }
