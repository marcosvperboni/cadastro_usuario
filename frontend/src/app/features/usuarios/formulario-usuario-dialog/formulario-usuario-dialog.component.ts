import { Component, Inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { UsuarioService } from '../../../core/services/usuario.service';
import { ApiError, Usuario } from '../../../core/models/usuario.model';

const TELEFONE_PATTERN = /^\(?\d{2}\)?[\s-]?\d{4,5}-?\d{4}$/;
const SENHA_PATTERN = /^(?=.*[A-Za-z])(?=.*\d).{8,}$/;

export interface FormularioUsuarioDialogData {
  usuario: Usuario | null;
}

@Component({
  selector: 'app-formulario-usuario-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './formulario-usuario-dialog.component.html',
})
export class FormularioUsuarioDialogComponent {
  readonly modoEdicao = !!this.data.usuario;
  readonly carregando = signal(false);
  readonly erro = signal<string | null>(null);
  ocultarSenha = true;

  readonly formulario = this.fb.group({
    nome: [this.data.usuario?.nome ?? '', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
    telefone: [
      this.data.usuario?.telefone ?? '',
      [Validators.required, Validators.pattern(TELEFONE_PATTERN)],
    ],
    senha: [
      '',
      this.modoEdicao ? [] : [Validators.required, Validators.pattern(SENHA_PATTERN)],
    ],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly usuarioService: UsuarioService,
    private readonly dialogRef: MatDialogRef<FormularioUsuarioDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: FormularioUsuarioDialogData
  ) {}

  salvar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.erro.set(null);
    this.carregando.set(true);
    const { nome, telefone, senha } = this.formulario.getRawValue();

    const requisicao = this.modoEdicao
      ? this.usuarioService.atualizar(this.data.usuario!.id, { nome: nome!, telefone: telefone! })
      : this.usuarioService.registrar({ nome: nome!, telefone: telefone!, senha: senha! });

    requisicao.subscribe({
      next: (usuario) => {
        this.carregando.set(false);
        this.dialogRef.close(usuario);
      },
      error: (erro: HttpErrorResponse) => {
        this.carregando.set(false);
        const apiError = erro.error as ApiError | undefined;
        this.erro.set(apiError?.mensagem ?? 'Não foi possível salvar o usuário.');
      },
    });
  }
}
