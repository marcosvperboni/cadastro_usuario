import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { debounceTime, Subject } from 'rxjs';
import { UsuarioService } from '../../../core/services/usuario.service';
import { AuthService } from '../../../core/services/auth.service';
import { Role, Usuario } from '../../../core/models/usuario.model';
import { FormularioUsuarioDialogComponent } from '../formulario-usuario-dialog/formulario-usuario-dialog.component';
import { ConfirmarDialogComponent } from '../../../shared/confirmar-dialog/confirmar-dialog.component';

@Component({
  selector: 'app-lista-usuarios',
  standalone: true,
  imports: [
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatSelectModule,
    MatChipsModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './lista-usuarios.component.html',
  styleUrl: './lista-usuarios.component.scss',
})
export class ListaUsuariosComponent implements OnInit {
  readonly colunas = ['nome', 'telefone', 'role', 'ativo', 'acoes'];
  readonly usuarios = signal<Usuario[]>([]);
  readonly carregando = signal(false);
  readonly totalRegistros = signal(0);
  readonly tamanhoPagina = signal(10);
  readonly paginaAtual = signal(0);
  readonly busca = signal('');

  private readonly buscaSubject = new Subject<string>();

  constructor(
    private readonly usuarioService: UsuarioService,
    private readonly authService: AuthService,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar
  ) {
    this.buscaSubject.pipe(debounceTime(400)).subscribe((valor) => {
      this.busca.set(valor);
      this.paginaAtual.set(0);
      this.carregarUsuarios();
    });
  }

  ngOnInit(): void {
    this.carregarUsuarios();
  }

  aoDigitarBusca(valor: string): void {
    this.buscaSubject.next(valor);
  }

  carregarUsuarios(): void {
    this.carregando.set(true);
    this.usuarioService
      .listar(this.paginaAtual(), this.tamanhoPagina(), this.busca())
      .subscribe({
        next: (pagina) => {
          this.usuarios.set(pagina.content);
          this.totalRegistros.set(pagina.totalElements);
          this.carregando.set(false);
        },
        error: () => this.carregando.set(false),
      });
  }

  aoMudarPagina(evento: PageEvent): void {
    this.paginaAtual.set(evento.pageIndex);
    this.tamanhoPagina.set(evento.pageSize);
    this.carregarUsuarios();
  }

  ehUsuarioLogado(usuario: Usuario): boolean {
    return this.authService.usuarioLogado()?.usuarioId === usuario.id;
  }

  novoUsuario(): void {
    const dialogRef = this.dialog.open(FormularioUsuarioDialogComponent, {
      data: { usuario: null },
      width: '420px',
    });

    dialogRef.afterClosed().subscribe((resultado) => {
      if (resultado) {
        this.snackBar.open('Usuário criado com sucesso!', 'Fechar', { duration: 3000 });
        this.carregarUsuarios();
      }
    });
  }

  editarUsuario(usuario: Usuario): void {
    const dialogRef = this.dialog.open(FormularioUsuarioDialogComponent, {
      data: { usuario },
      width: '420px',
    });

    dialogRef.afterClosed().subscribe((resultado) => {
      if (resultado) {
        this.snackBar.open('Usuário atualizado com sucesso!', 'Fechar', { duration: 3000 });
        this.carregarUsuarios();
      }
    });
  }

  alterarRole(usuario: Usuario, novaRole: Role): void {
    if (novaRole === usuario.role) {
      return;
    }

    this.usuarioService.alterarRole(usuario.id, novaRole).subscribe({
      next: () => {
        this.snackBar.open('Perfil de acesso atualizado!', 'Fechar', { duration: 3000 });
        this.carregarUsuarios();
      },
      error: () => this.carregarUsuarios(),
    });
  }

  excluirUsuario(usuario: Usuario): void {
    const dialogRef = this.dialog.open(ConfirmarDialogComponent, {
      data: {
        titulo: 'Excluir usuário',
        mensagem: `Deseja realmente excluir o usuário "${usuario.nome}"? Esta ação não pode ser desfeita.`,
        textoConfirmar: 'Excluir',
      },
    });

    dialogRef.afterClosed().subscribe((confirmado) => {
      if (confirmado) {
        this.usuarioService.excluir(usuario.id).subscribe({
          next: () => {
            this.snackBar.open('Usuário excluído com sucesso!', 'Fechar', { duration: 3000 });
            this.carregarUsuarios();
          },
          error: () => {
            this.snackBar.open('Não foi possível excluir o usuário.', 'Fechar', { duration: 3000 });
          },
        });
      }
    });
  }
}
