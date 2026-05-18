import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewspaperFormComponent } from './newspaper-form.component';

describe('NewspaperFormComponent', () => {
  let component: NewspaperFormComponent;
  let fixture: ComponentFixture<NewspaperFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NewspaperFormComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(NewspaperFormComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
