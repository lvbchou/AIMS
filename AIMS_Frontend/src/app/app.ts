import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Header } from './components/header/header';
import { Footer } from './components/footer/footer';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Header, Footer],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
/**
 * Coupling: Data coupling through Angular routing and focused layout components.
 * Cohesion: Functional cohesion because it provides the application layout shell.
 */
export class App {
  protected readonly title = signal('frontend');
}
