import { Component, Input, OnInit } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dvd-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './dvd-form.component.html',
})
export class DvdFormComponent {
  @Input() form!: FormGroup;
}