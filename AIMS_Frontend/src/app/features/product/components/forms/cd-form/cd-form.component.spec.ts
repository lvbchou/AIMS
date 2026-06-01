import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CdFormComponent } from './cd-form.component';

describe('CdFormComponent', () => {
  let component: CdFormComponent;
  let fixture: ComponentFixture<CdFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CdFormComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(CdFormComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
