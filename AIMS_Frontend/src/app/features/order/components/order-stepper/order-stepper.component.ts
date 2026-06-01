import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-order-stepper',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './order-stepper.component.html',
  styleUrl: './order-stepper.component.scss'
})
/**
 * Coupling: Data coupling through a numeric currentStep input.
 * Cohesion: Functional cohesion because it displays checkout progress only.
 */
export class OrderStepperComponent {
  @Input() currentStep: number = 1;
}
