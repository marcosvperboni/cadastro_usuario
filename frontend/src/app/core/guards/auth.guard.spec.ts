import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';

describe('authGuard', () => {
  let authServiceMock: { estaAutenticado: jasmine.Spy };
  let routerMock: { parseUrl: jasmine.Spy };

  beforeEach(() => {
    authServiceMock = { estaAutenticado: jasmine.createSpy('estaAutenticado') };
    routerMock = { parseUrl: jasmine.createSpy('parseUrl').and.returnValue('urlTree-login') };

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: Router, useValue: routerMock },
      ],
    });
  });

  function executarGuard() {
    return TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));
  }

  it('deve permitir acesso quando autenticado', () => {
    authServiceMock.estaAutenticado.and.returnValue(true);
    expect(executarGuard()).toBeTrue();
  });

  it('deve redirecionar para login quando nao autenticado', () => {
    authServiceMock.estaAutenticado.and.returnValue(false);
    expect(executarGuard()).toBe('urlTree-login' as never);
    expect(routerMock.parseUrl).toHaveBeenCalledWith('/login');
  });
});
