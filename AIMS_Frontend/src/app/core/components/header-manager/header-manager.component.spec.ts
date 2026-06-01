import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HeaderManagerComponent } from './header-manager.component';

describe('HeaderManagerComponent', () => {
  let component: HeaderManagerComponent;
  let fixture: ComponentFixture<HeaderManagerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HeaderManagerComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(HeaderManagerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render header element', () => {
    const headerElement = fixture.nativeElement.querySelector('header');
    expect(headerElement).toBeTruthy();
  });

  it('should have header with correct CSS class', () => {
    const headerElement = fixture.nativeElement.querySelector('header');
    expect(headerElement.classList.contains('header')).toBeTruthy();
  });

  it('should display "Manager Dashboard" text', () => {
    const h1Element = fixture.nativeElement.querySelector('h1');
    expect(h1Element.textContent).toBe('Manager Dashboard');
  });

  it('should render h1 element inside header', () => {
    const headerElement = fixture.nativeElement.querySelector('header');
    const h1Element = headerElement.querySelector('h1');
    expect(h1Element).toBeTruthy();
  });
});
