import { Injectable, signal } from '@angular/core';

export interface DialogConfig {
  title: string;
  message: string;
  onConfirm: () => void;
}

@Injectable({ providedIn: 'root' })
export class DialogService {
  // Signal chứa config hiện tại — null nghĩa là đang đóng
  readonly config = signal<DialogConfig | null>(null);

  open(config: DialogConfig): void {
    this.config.set(config);
  }

  close(): void {
    this.config.set(null);
  }

  confirm(): void {
    const current = this.config();
    if (current) {
      current.onConfirm();
      this.close();
    }
  }
}