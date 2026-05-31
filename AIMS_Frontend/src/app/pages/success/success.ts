import { Component } from '@angular/core';

@Component({
  selector: 'app-success',
  standalone: true,
  templateUrl: './success.html',
  styleUrl: './success.css'
})
/**
 * Coupling: Data coupling limited to displayed order-confirmation data.
 * Cohesion: Functional cohesion because it displays checkout completion only.
 */
export class Success {
  orderReference = '#AIMS-82910-SUC';
}
