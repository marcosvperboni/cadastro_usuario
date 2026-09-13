export type Role = 'ADMIN' | 'USER';

export interface Usuario {
  id: number;
  nome: string;
  telefone: string;
  role: Role;
  ativo: boolean;
  criadoEm: string;
}

export interface UsuarioRequest {
  nome: string;
  telefone: string;
  senha: string;
}

export interface UsuarioUpdateRequest {
  nome: string;
  telefone: string;
}

export interface PaginaUsuarios {
  content: Usuario[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface LoginRequest {
  telefone: string;
  senha: string;
}

export interface LoginResponse {
  token: string;
  tipo: string;
  usuarioId: number;
  nome: string;
  role: Role;
}

export interface ApiError {
  timestamp: string;
  status: number;
  erro: string;
  mensagem: string;
  detalhes: string[];
}
