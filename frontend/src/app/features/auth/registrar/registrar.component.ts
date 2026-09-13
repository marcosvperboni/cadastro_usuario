import { Component, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UsuarioService } from '../../../core/services/usuario.service';
import { ApiError } from '../../../core/models/usuario.model';

const TELEFONE_PATTERN = /^\(?\d{2}\)?[\s-]?\d{4,5}-?\d{4}$/;
const SENHA_PATTERN = /^(?=.*[A-Za-z])(?=.*\d).{8,}$/;

function senhasIguaisValidator(): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const senha = grupo.get('senha')?.value;
    const confirmarSenha = grupo.get('confirmarSenha')?.value;
    return senha === confirmarSenha ? null : { senhasDiferentes: true };
  };
}

@Component({
  selector: 'app-registrar',
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
  templateUrl: './registrar.component.html',
  styleUrl: './registrar.component.scss',
})
export class RegistrarComponent {
  readonly carregando = signal(false);
  readonly erro = signal<string | null>(null);
  ocultarSenha = true;

  readonly formulario = this.fb.group(
    {
      nome: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
      telefone: ['', [Validators.required, Validators.pattern(TELEFONE_PATTERN)]],
      senha: ['', [Validators.required, Validators.pattern(SENHA_PATTERN)]],
      confirmarSenha: ['', [Validators.required]],
    },
    { validators: senhasIguaisValidator() }
  );

  constructor(
    private readonly fb: FormBuilder,
    private readonly usuarioService: UsuarioService,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar
  ) {}

  registrar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.erro.set(null);
    this.carregando.set(true);

    const { nome, telefone, senha } = this.formulario.getRawValue();
    this.usuarioService.registrar({ nome: nome!, telefone: telefone!, senha: senha! }).subscribe({
      next: () => {
        this.carregando.set(false);
        this.snackBar.open('Cadastro realizado com sucesso! Faça login para continuar.', 'Fechar', {
          duration: 5000,
        });
        this.router.navigate(['/login']);
      },
      error: (erro: HttpErrorResponse) => {
        this.carregando.set(false);
        const apiError = erro.error as ApiError | undefined;
        this.erro.set(apiError?.mensagem ?? 'Não foi possível concluir o cadastro. Tente novamente.');
      },
    });
  }
}
