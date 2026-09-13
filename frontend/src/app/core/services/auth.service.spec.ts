import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('deve iniciar sem usuario autenticado quando nao ha sessao salva', () => {
    expect(service.estaAutenticado()).toBeFalse();
    expect(service.usuarioLogado()).toBeNull();
  });

  it('deve autenticar e persistir a sessao no login', () => {
    service.login({ telefone: '(11) 99999-9999', senha: 'Admin@123' }).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush({ token: 'token-fake', tipo: 'Bearer', usuarioId: 1, nome: 'Admin', role: 'ADMIN' });

    expect(service.estaAutenticado()).toBeTrue();
    expect(service.ehAdmin()).toBeTrue();
    expect(service.obterToken()).toBe('token-fake');
  });

  it('deve limpar a sessao no logout', () => {
    service.login({ telefone: '(11) 99999-9999', senha: 'Admin@123' }).subscribe();
    httpMock
      .expectOne(`${environment.apiUrl}/auth/login`)
      .flush({ token: 'token-fake', tipo: 'Bearer', usuarioId: 1, nome: 'Admin', role: 'ADMIN' });

    service.logout();

    expect(service.estaAutenticado()).toBeFalse();
    expect(service.obterToken()).toBeNull();
  });

  it('deve chamar o endpoint de esqueci-senha', () => {
    service.esqueciSenha('(11) 99999-9999').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/esqueci-senha`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ telefone: '(11) 99999-9999' });
    req.flush(null);
  });

  it('deve chamar o endpoint de redefinir-senha', () => {
    service.redefinirSenha('token-abc', 'novaSenha123').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/redefinir-senha`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ token: 'token-abc', novaSenha: 'novaSenha123' });
    req.flush(null);
  });
});
