import { Component, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth.service';
import { ApiError } from '../../../core/models/usuario.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  readonly carregando = signal(false);
  readonly erro = signal<string | null>(null);
  ocultarSenha = true;

  readonly formulario = this.fb.group({
    telefone: ['', [Validators.required]],
    senha: ['', [Validators.required]],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  entrar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.erro.set(null);
    this.carregando.set(true);

    const { telefone, senha } = this.formulario.getRawValue();
    this.authService.login({ telefone: telefone!, senha: senha! }).subscribe({
      next: () => {
        this.carregando.set(false);
        this.router.navigate(['/perfil']);
      },
      error: (erro: HttpErrorResponse) => {
        this.carregando.set(false);
        const apiError = erro.error as ApiError | undefined;
        this.erro.set(apiError?.mensagem ?? 'Não foi possível efetuar o login. Tente novamente.');
      },
    });
  }
}
