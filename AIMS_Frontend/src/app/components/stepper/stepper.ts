import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-stepper',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './stepper.html',
  styleUrl: './stepper.css'
})
/**
 * Coupling: Data coupling through a numeric currentStep input.
 * Cohesion: Functional cohesion because it displays checkout progress only.
 */
export class Stepper {
  @Input() currentStep: number = 1;
}
