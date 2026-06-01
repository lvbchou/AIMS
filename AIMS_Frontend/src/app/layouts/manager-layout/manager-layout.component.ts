import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderManagerComponent } from '../../core/components/header-manager/header-manager.component';
import { FooterComponent } from '../../core/components/footer/footer.component';

@Component({
  selector: 'app-manager-layout',
  standalone: true,
  imports: [RouterOutlet, HeaderManagerComponent, FooterComponent],
  templateUrl: './manager-layout.component.html'
})
export class ManagerLayoutComponent {}