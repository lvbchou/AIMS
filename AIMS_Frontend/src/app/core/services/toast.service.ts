import { Injectable, signal } from '@angular/core';

export interface Toast {
  id: number;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly toasts = signal<Toast[]>([]);

  private nextId = 0;
  private duration = 3000; // ms

  show(message: string): void {
    const id = this.nextId++;

    // Thêm toast vào queue
    this.toasts.update(list => [...list, { id, message }]);

    // Tự động xóa sau duration
    setTimeout(() => this.remove(id), this.duration);
  }

  remove(id: number): void {
    this.toasts.update(list => list.filter(t => t.id !== id));
  }
}