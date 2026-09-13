import { Component, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/services/auth.service';
import { ApiError } from '../../../core/models/usuario.model';

const SENHA_PATTERN = /^(?=.*[A-Za-z])(?=.*\d).{8,}$/;

function senhasIguaisValidator(): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const novaSenha = grupo.get('novaSenha')?.value;
    const confirmarSenha = grupo.get('confirmarSenha')?.value;
    return novaSenha === confirmarSenha ? null : { senhasDiferentes: true };
  };
}

@Component({
  selector: 'app-redefinir-senha',
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
  templateUrl: './redefinir-senha.component.html',
  styleUrl: './redefinir-senha.component.scss',
})
export class RedefinirSenhaComponent {
  readonly carregando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly sucesso = signal(false);
  ocultarSenha = true;

  readonly formulario = this.fb.group(
    {
      token: ['', [Validators.required]],
      novaSenha: ['', [Validators.required, Validators.pattern(SENHA_PATTERN)]],
      confirmarSenha: ['', [Validators.required]],
    },
    { validators: senhasIguaisValidator() }
  );

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router,
    route: ActivatedRoute
  ) {
    const tokenNaUrl = route.snapshot.queryParamMap.get('token');
    if (tokenNaUrl) {
      this.formulario.patchValue({ token: tokenNaUrl });
    }
  }

  redefinir(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.erro.set(null);
    this.carregando.set(true);

    const { token, novaSenha } = this.formulario.getRawValue();
    this.authService.redefinirSenha(token!, novaSenha!).subscribe({
      next: () => {
        this.carregando.set(false);
        this.sucesso.set(true);
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: (erro: HttpErrorResponse) => {
        this.carregando.set(false);
        const apiError = erro.error as ApiError | undefined;
        this.erro.set(apiError?.mensagem ?? 'Não foi possível redefinir a senha.');
      },
    });
  }
}
