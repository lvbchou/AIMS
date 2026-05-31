import { Component } from '@angular/core';

@Component({
  selector: 'app-footer',
  imports: [],
  templateUrl: './footer.html',
  styleUrl: './footer.css',
})
/**
 * Coupling: Data coupling limited to Angular rendering configuration.
 * Cohesion: Functional cohesion because it renders the shared footer only.
 */
export class Footer {}
