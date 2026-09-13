import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'perfil' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'esqueci-senha',
    loadComponent: () =>
      import('./features/auth/esqueci-senha/esqueci-senha.component').then((m) => m.EsqueciSenhaComponent),
  },
  {
    path: 'redefinir-senha',
    loadComponent: () =>
      import('./features/auth/redefinir-senha/redefinir-senha.component').then(
        (m) => m.RedefinirSenhaComponent
      ),
  },
  {
    path: 'registrar',
    loadComponent: () =>
      import('./features/auth/registrar/registrar.component').then((m) => m.RegistrarComponent),
  },
  {
    path: 'perfil',
    canActivate: [authGuard],
    loadComponent: () => import('./features/usuarios/perfil/perfil.component').then((m) => m.PerfilComponent),
  },
  {
    path: 'usuarios',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./features/usuarios/lista-usuarios/lista-usuarios.component').then(
        (m) => m.ListaUsuariosComponent
      ),
  },
  { path: '**', redirectTo: 'perfil' },
];
