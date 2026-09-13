import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  PaginaUsuarios,
  Role,
  Usuario,
  UsuarioRequest,
  UsuarioUpdateRequest,
} from '../models/usuario.model';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly baseUrl = `${environment.apiUrl}/usuarios`;

  constructor(private readonly http: HttpClient) {}

  registrar(request: UsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.baseUrl}/registrar`, request);
  }

  listar(pagina: number, tamanho: number, busca: string): Observable<PaginaUsuarios> {
    let params = new HttpParams().set('page', pagina).set('size', tamanho);
    if (busca) {
      params = params.set('busca', busca);
    }
    return this.http.get<PaginaUsuarios>(this.baseUrl, { params });
  }

  buscarPorId(id: number): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.baseUrl}/${id}`);
  }

  buscarPerfilLogado(): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.baseUrl}/me`);
  }

  atualizar(id: number, request: UsuarioUpdateRequest): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.baseUrl}/${id}`, request);
  }

  alterarRole(id: number, role: Role): Observable<Usuario> {
    return this.http.patch<Usuario>(`${this.baseUrl}/${id}/role`, { role });
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
