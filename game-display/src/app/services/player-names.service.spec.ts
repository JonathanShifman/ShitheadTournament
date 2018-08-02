import { TestBed, inject } from '@angular/core/testing';

import { PlayerNamesService } from './player-names.service';

describe('PlayerNamesService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PlayerNamesService]
    });
  });

  it('should be created', inject([PlayerNamesService], (service: PlayerNamesService) => {
    expect(service).toBeTruthy();
  }));
});
