import { Component, OnInit, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UsuarioService } from '../../../core/services/usuario.service';
import { Usuario } from '../../../core/models/usuario.model';
import { FormularioUsuarioDialogComponent } from '../formulario-usuario-dialog/formulario-usuario-dialog.component';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [MatCardModule, MatButtonModule, MatIconModule, MatChipsModule],
  templateUrl: './perfil.component.html',
  styleUrl: './perfil.component.scss',
})
export class PerfilComponent implements OnInit {
  readonly usuario = signal<Usuario | null>(null);
  readonly carregando = signal(true);

  constructor(
    private readonly usuarioService: UsuarioService,
    private readonly dialog: MatDialog,
    private readonly snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.carregarPerfil();
  }

  carregarPerfil(): void {
    this.carregando.set(true);
    this.usuarioService.buscarPerfilLogado().subscribe({
      next: (usuario) => {
        this.usuario.set(usuario);
        this.carregando.set(false);
      },
      error: () => this.carregando.set(false),
    });
  }

  editar(): void {
    const usuarioAtual = this.usuario();
    if (!usuarioAtual) {
      return;
    }

    const dialogRef = this.dialog.open(FormularioUsuarioDialogComponent, {
      data: { usuario: usuarioAtual },
      width: '420px',
    });

    dialogRef.afterClosed().subscribe((resultado) => {
      if (resultado) {
        this.usuario.set(resultado);
        this.snackBar.open('Dados atualizados com sucesso!', 'Fechar', { duration: 3000 });
      }
    });
  }
}
