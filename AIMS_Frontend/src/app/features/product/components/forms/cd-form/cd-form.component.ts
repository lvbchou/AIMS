import { Component, Input } from '@angular/core';
import { FormGroup, FormArray, FormControl, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-cd-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './cd-form.component.html',
})
export class CdFormComponent {
  @Input() form!: FormGroup;

  constructor(private fb: FormBuilder) {}

  get artists(): FormArray {
    return this.form.get('artists') as FormArray;
  }

  get tracklist(): FormArray {
    return this.form.get('tracklist') as FormArray;
  }

  addArtist(): void {
    this.artists.push(new FormControl('', Validators.required));
  }

  removeArtist(index: number): void {
    if (this.artists.length > 1) {
      this.artists.removeAt(index);
    }
  }

  addTrack(): void {
    this.tracklist.push(
      this.fb.group({
        title:  ['', Validators.required],
        length: ['', Validators.required],
      })
    );
  }

  removeTrack(index: number): void {
    this.tracklist.removeAt(index);
  }
}