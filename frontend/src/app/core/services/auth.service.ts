import { HttpClient } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, Role } from '../models/usuario.model';

const CHAVE_TOKEN = 'cadastro-usuario:token';
const CHAVE_USUARIO = 'cadastro-usuario:usuario';

interface UsuarioSessao {
  usuarioId: number;
  nome: string;
  role: Role;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly baseUrl = `${environment.apiUrl}/auth`;

  private readonly usuarioSessao = signal<UsuarioSessao | null>(this.carregarSessao());

  readonly usuarioLogado = computed(() => this.usuarioSessao());
  readonly estaAutenticado = computed(() => this.usuarioSessao() !== null);
  readonly ehAdmin = computed(() => this.usuarioSessao()?.role === 'ADMIN');

  constructor(private readonly http: HttpClient) {}

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/login`, request).pipe(
      tap((resposta) => this.persistirSessao(resposta))
    );
  }

  esqueciSenha(telefone: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/esqueci-senha`, { telefone });
  }

  redefinirSenha(token: string, novaSenha: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/redefinir-senha`, { token, novaSenha });
  }

  logout(): void {
    localStorage.removeItem(CHAVE_TOKEN);
    localStorage.removeItem(CHAVE_USUARIO);
    this.usuarioSessao.set(null);
  }

  obterToken(): string | null {
    return localStorage.getItem(CHAVE_TOKEN);
  }

  private persistirSessao(resposta: LoginResponse): void {
    const sessao: UsuarioSessao = {
      usuarioId: resposta.usuarioId,
      nome: resposta.nome,
      role: resposta.role,
    };
    localStorage.setItem(CHAVE_TOKEN, resposta.token);
    localStorage.setItem(CHAVE_USUARIO, JSON.stringify(sessao));
    this.usuarioSessao.set(sessao);
  }

  private carregarSessao(): UsuarioSessao | null {
    const bruto = localStorage.getItem(CHAVE_USUARIO);
    if (!bruto) {
      return null;
    }
    try {
      return JSON.parse(bruto) as UsuarioSessao;
    } catch {
      return null;
    }
  }
}
