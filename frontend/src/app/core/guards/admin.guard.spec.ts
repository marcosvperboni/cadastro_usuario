import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { adminGuard } from './admin.guard';
import { AuthService } from '../services/auth.service';

describe('adminGuard', () => {
  let authServiceMock: { estaAutenticado: jasmine.Spy; ehAdmin: jasmine.Spy };
  let routerMock: { parseUrl: jasmine.Spy };

  beforeEach(() => {
    authServiceMock = {
      estaAutenticado: jasmine.createSpy('estaAutenticado'),
      ehAdmin: jasmine.createSpy('ehAdmin'),
    };
    routerMock = { parseUrl: jasmine.createSpy('parseUrl').and.callFake((url: string) => url) };

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: Router, useValue: routerMock },
      ],
    });
  });

  function executarGuard() {
    return TestBed.runInInjectionContext(() => adminGuard({} as never, {} as never));
  }

  it('deve permitir acesso quando autenticado e administrador', () => {
    authServiceMock.estaAutenticado.and.returnValue(true);
    authServiceMock.ehAdmin.and.returnValue(true);

    expect(executarGuard()).toBeTrue();
  });

  it('deve redirecionar usuario comum autenticado para o perfil', () => {
    authServiceMock.estaAutenticado.and.returnValue(true);
    authServiceMock.ehAdmin.and.returnValue(false);

    expect(executarGuard()).toBe('/perfil' as never);
  });

  it('deve redirecionar usuario nao autenticado para o login', () => {
    authServiceMock.estaAutenticado.and.returnValue(false);
    authServiceMock.ehAdmin.and.returnValue(false);

    expect(executarGuard()).toBe('/login' as never);
  });
});
