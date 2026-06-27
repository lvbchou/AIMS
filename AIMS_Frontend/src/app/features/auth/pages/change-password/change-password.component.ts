import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ChangePasswordService, ChangePasswordPayload } from '../../../../core/services/auth/change-password.service';

/**
 * Change Password modal-style page.
 *
 * Rendered at route /change-password — uses position:fixed overlay so it visually
 * sits on top of the manager layout without any parent component modification.
 *
 * Responsibilities (SRP):
 *   - Render the 3-field form
 *   - Delegate HTTP to ChangePasswordService (not auth.service.ts — OCP)
 *   - Show inline validation feedback before sending (client-side pre-check)
 *   - Navigate back on success or cancel
 *
 * No existing component, service, or guard was modified.
 */
@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './change-password.component.html',
  styleUrl:    './change-password.component.scss'
})
export class ChangePasswordComponent {

  currentPassword  = '';
  newPassword      = '';
  confirmPassword  = '';

  loading  = false;
  error    = '';
  success  = false;

  /** Track which field was blurred so inline hints appear progressively. */
  touched = { current: false, newPwd: false, confirm: false };

  constructor(
    private changePasswordService: ChangePasswordService,
    private router: Router
  ) {}

  // ── Inline validation helpers (client-side, UX only — backend re-validates) ──

  get newPasswordHint(): string {
    if (!this.touched.newPwd || !this.newPassword) return '';
    if (this.newPassword.length < 8)          return 'At least 8 characters required.';
    if (!/[A-Z]/.test(this.newPassword))      return 'Must include an uppercase letter.';
    if (!/[a-z]/.test(this.newPassword))      return 'Must include a lowercase letter.';
    if (!/[0-9]/.test(this.newPassword))      return 'Must include a digit.';
    if (this.newPassword === this.currentPassword) return 'Must differ from current password.';
    return '';
  }

  get confirmHint(): string {
    if (!this.touched.confirm || !this.confirmPassword) return '';
    return this.newPassword !== this.confirmPassword ? 'Passwords do not match.' : '';
  }

  get formInvalid(): boolean {
    return !this.currentPassword
        || !this.newPassword
        || !this.confirmPassword
        || !!this.newPasswordHint
        || !!this.confirmHint;
  }

  // ── Actions ──────────────────────────────────────────────────────────────────

  submit(): void {
    if (this.formInvalid || this.loading) return;

    this.loading = true;
    this.error   = '';
    this.success = false;

    const payload: ChangePasswordPayload = {
      currentPassword: this.currentPassword,
      newPassword:     this.newPassword,
      confirmPassword: this.confirmPassword
    };

    this.changePasswordService.changePassword(payload).subscribe({
      next: () => {
        this.loading = false;
        this.success = true;
        // Show success message briefly then navigate to appropriate dashboard
        const target = this.router.url.startsWith('/admin') ? '/admin' : '/product-manager';
        setTimeout(() => this.router.navigate([target]), 1800);
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message ?? 'An error occurred. Please try again.';
      }
    });
  }

  close(): void {
    const target = this.router.url.startsWith('/admin') ? '/admin' : '/product-manager';
    this.router.navigate([target]);
  }
}
