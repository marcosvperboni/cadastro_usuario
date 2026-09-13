import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { UsuarioService } from './usuario.service';
import { environment } from '../../../environments/environment';

describe('UsuarioService', () => {
  let service: UsuarioService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(UsuarioService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('deve enviar POST para registrar um novo usuario', () => {
    service.registrar({ nome: 'Ana', telefone: '(11) 91234-5678', senha: 'senha1234' }).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/usuarios/registrar`);
    expect(req.request.method).toBe('POST');
    req.flush({ id: 1, nome: 'Ana', telefone: '(11) 91234-5678', role: 'USER', ativo: true, criadoEm: '' });
  });

  it('deve enviar GET com parametros de paginacao e busca', () => {
    service.listar(1, 10, 'Ana').subscribe();

    const req = httpMock.expectOne(
      (r) => r.url === environment.apiUrl + '/usuarios' && r.params.get('busca') === 'Ana'
    );
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('size')).toBe('10');
    req.flush({ content: [], totalElements: 0, totalPages: 0, number: 1, size: 10 });
  });

  it('deve enviar PUT ao atualizar usuario', () => {
    service.atualizar(1, { nome: 'Ana Souza', telefone: '(11) 91234-5678' }).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/usuarios/1`);
    expect(req.request.method).toBe('PUT');
    req.flush({ id: 1, nome: 'Ana Souza', telefone: '(11) 91234-5678', role: 'USER', ativo: true, criadoEm: '' });
  });

  it('deve enviar PATCH ao alterar role', () => {
    service.alterarRole(1, 'ADMIN').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/usuarios/1/role`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ role: 'ADMIN' });
    req.flush({ id: 1, nome: 'Ana', telefone: '(11) 91234-5678', role: 'ADMIN', ativo: true, criadoEm: '' });
  });

  it('deve enviar DELETE ao excluir usuario', () => {
    service.excluir(1).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/usuarios/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
