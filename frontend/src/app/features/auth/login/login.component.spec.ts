import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthService } from '../../../core/services/auth.service';

describe('LoginComponent', () => {
  let authServiceMock: { login: jasmine.Spy };

  beforeEach(async () => {
    authServiceMock = { login: jasmine.createSpy('login') };

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [provideRouter([]), { provide: AuthService, useValue: authServiceMock }],
    }).compileComponents();
  });

  it('nao deve chamar o servico quando o formulario e invalido', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.componentInstance.entrar();

    expect(authServiceMock.login).not.toHaveBeenCalled();
  });

  it('deve autenticar e navegar para o perfil quando as credenciais sao validas', () => {
    authServiceMock.login.and.returnValue(
      of({ token: 'x', tipo: 'Bearer', usuarioId: 1, nome: 'Ana', role: 'USER' })
    );

    const fixture = TestBed.createComponent(LoginComponent);
    const componente = fixture.componentInstance;
    const router = TestBed.inject(Router);
    const navegarSpy = spyOn(router, 'navigate');

    componente.formulario.setValue({ telefone: '(11) 99999-9999', senha: 'Admin@123' });
    componente.entrar();

    expect(authServiceMock.login).toHaveBeenCalledWith({
      telefone: '(11) 99999-9999',
      senha: 'Admin@123',
    });
    expect(navegarSpy).toHaveBeenCalledWith(['/perfil']);
  });

  it('deve exibir mensagem de erro quando o login falha', () => {
    authServiceMock.login.and.returnValue(
      throwError(() => ({ error: { mensagem: 'Telefone ou senha invalidos' } }))
    );

    const fixture = TestBed.createComponent(LoginComponent);
    const componente = fixture.componentInstance;
    componente.formulario.setValue({ telefone: '(11) 99999-9999', senha: 'errada' });

    componente.entrar();

    expect(componente.erro()).toBe('Telefone ou senha invalidos');
  });
});
