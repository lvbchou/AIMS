import { Component } from '@angular/core';

@Component({
  selector: 'app-header',
  imports: [],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
/**
 * Coupling: Data coupling limited to Angular rendering configuration.
 * Cohesion: Functional cohesion because it renders the shared header only.
 */
export class Header {}
