import { Component, Input } from '@angular/core';
import { FormArray, FormControl, FormGroup, ReactiveFormsModule,  } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-newspaper-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './newspaper-form.component.html',
})
export class NewspaperFormComponent {
  @Input() form!: FormGroup;

  get sections(): FormArray {
    return this.form.get('sections') as FormArray;
  }

  addSection(): void {
    this.sections.push(new FormControl(''));
  }

  removeSection(index: number): void {
    if (this.sections.length > 1) {
      this.sections.removeAt(index);
    }
    this.sections.removeAt(index);
  }
}