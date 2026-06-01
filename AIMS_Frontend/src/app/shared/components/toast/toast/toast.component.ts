import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../../../core/services/toast/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './toast.component.html',
  styleUrls: ['./toast.component.scss'],
})
export class ToastComponent {
  readonly toastService = inject(ToastService);

  get toasts() {
    return this.toastService.toasts();
  }

  remove(id: number): void {
    this.toastService.remove(id);
  }
}