import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DialogService } from '../../../../core/services/dialog/dialog.service';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirm-dialog.component.html',
  styleUrls: ['./confirm-dialog.component.scss'],
})
export class ConfirmDialogComponent {
  readonly dialogService = inject(DialogService);

  get config() {
    return this.dialogService.config();
  }

  onConfirm(): void {
    this.dialogService.confirm();
  }

  onCancel(): void {
    this.dialogService.close();
  }
}