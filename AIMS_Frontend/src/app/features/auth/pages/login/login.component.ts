import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../../core/services/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {

  username = '';
  password = '';

  loading = false;
  error = '';

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  login(): void {

      this.loading = true;
      this.error = '';

      this.authService.login({
          username: this.username,
          password: this.password
      })
      .subscribe({
          next: response => {
               console.log('Login successful:', response); 
              localStorage.setItem('token', response.token);
              localStorage.setItem('roles', JSON.stringify(response.roles));
              localStorage.setItem('username', response.username);
              if(response.roles.includes('ADMIN')){
                  this.router.navigate(['/admin']);
              } else {
                  this.router.navigate(['/product-manager']);
              }
              this.loading = false;
          },
          error: () => {
              this.error = 'Incorrect username or password';
              this.loading = false;
              this.cdr.detectChanges();
          }
      });
  }
}