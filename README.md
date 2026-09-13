# Cadastro de Usuários

Sistema completo de cadastro de usuários com autenticação por telefone, recuperação de senha, controle de permissões (RBAC) e CRUD completo, desenvolvido como projeto de portfólio seguindo boas práticas de mercado: **Clean Code**, **DDD (Domain-Driven Design)**, **TDD**, padrões de projeto e CI/CD.

## Funcionalidades

- **Login por telefone e senha**, com autenticação via JWT
- **Recuperação de senha** por token (esqueci minha senha / redefinir senha)
- **Controle de permissões de acesso** (perfis `ADMIN` e `USER`)
  - `USER`: acessa e edita apenas o próprio cadastro
  - `ADMIN`: gerencia todos os usuários (criar, listar, editar, excluir e alterar perfis de acesso)
- **CRUD completo de usuários** persistido em **PostgreSQL** (inserção, edição e exclusão reais no banco)
- **Senhas criptografadas** com BCrypt (nunca armazenadas em texto plano)
- **Validações completas** em todos os endpoints (`GET`, `POST`, `PUT`, `DELETE`, `PATCH`)
- **Tema claro/escuro** com troca em tempo real, incluindo adaptação dos ícones
- **Testes automatizados** no backend (JUnit + Mockito + MockMvc) e no frontend (Jasmine/Karma)

## Tecnologias

### Backend
- **Java 17**
- **Spring Boot 3** (Web, Data JPA, Security, Validation, Actuator)
- **Spring Security** com autenticação **JWT** (biblioteca `jjwt`)
- **BCrypt** para criptografia de senhas
- **PostgreSQL** como banco de dados
- **Flyway** para migrations versionadas do banco
- **JUnit 5 + Mockito** (testes unitários) e **MockMvc** (testes de integração dos controllers, com **H2** em memória)
- **Maven** como gerenciador de build

### Frontend
- **Angular 17** (standalone components, signals, control flow `@if`/`@for`)
- **Angular Material** (tema customizado com suporte a modo claro/escuro)
- **RxJS**, formulários reativos (`ReactiveFormsModule`) com validações
- **Jasmine/Karma** para testes unitários

### Infraestrutura
- **Podman** (Containerfiles + `podman-compose.yml` para orquestrar banco, backend e frontend)
- **GitHub Actions** para CI/CD (build e testes automatizados a cada push/PR)

## Arquitetura

O backend segue uma organização inspirada em **Clean Architecture / DDD**, separando responsabilidades em camadas:

```
backend/src/main/java/com/marcosperboni/cadastro_usuario/
├── domain/           # Entidades e contratos de repositório (regra de negócio pura)
├── application/      # Casos de uso (services), DTOs e exceções de negócio
├── infrastructure/   # Segurança (JWT, Spring Security)
└── web/              # Controllers REST e tratamento global de exceções
```

O frontend organiza-se por *feature*:

```
frontend/src/app/
├── core/             # Services, guards, interceptors e models compartilhados
├── features/
│   ├── auth/         # Login, cadastro, esqueci/redefinir senha
│   └── usuarios/     # Perfil do usuário logado e administração de usuários
└── shared/           # Componentes reutilizáveis (ex: diálogo de confirmação)
```

## Como executar

### Opção 1 — Com Podman (recomendado)

Pré-requisito: [Podman](https://podman.io/) instalado.

```bash
podman compose -f podman-compose.yml up --build
```

Isso sobe três serviços:
- **PostgreSQL** na porta `5432`
- **Backend** (Spring Boot) na porta `8080`
- **Frontend** (Angular servido via Nginx, com proxy reverso para `/api`) na porta `4200`

Acesse `http://localhost:4200`.

### Opção 2 — Manualmente (desenvolvimento)

**Backend** (requer um PostgreSQL disponível — ajuste as variáveis de ambiente conforme necessário):

```bash
cd backend
DB_HOST=localhost DB_PORT=5432 DB_NAME=cadastro_usuario DB_USER=cadastro_usuario DB_PASSWORD=cadastro_usuario \
  mvn spring-boot:run
```

**Frontend**:

```bash
cd frontend
npm install
npm start
```

Acesse `http://localhost:4200` (o frontend aponta para `http://localhost:8080/api` em desenvolvimento).

## Usuário administrador padrão

Uma migration do Flyway já cria um usuário administrador para facilitar o primeiro acesso:

| Telefone (login)   | Senha       |
|---------------------|-------------|
| `(11) 99999-9999`   | `Admin@123` |

> Troque essa senha (ou remova a migration de seed) antes de qualquer uso além de desenvolvimento/demonstração.

## Principais endpoints da API

| Método | Endpoint                          | Acesso           | Descrição                          |
|--------|------------------------------------|------------------|-------------------------------------|
| POST   | `/api/auth/login`                  | Público          | Autentica e retorna um token JWT    |
| POST   | `/api/auth/esqueci-senha`          | Público          | Gera token de redefinição de senha  |
| POST   | `/api/auth/redefinir-senha`        | Público          | Redefine a senha usando o token     |
| POST   | `/api/usuarios/registrar`          | Público          | Cria uma nova conta de usuário      |
| GET    | `/api/usuarios`                    | ADMIN            | Lista usuários (paginado, com busca)|
| GET    | `/api/usuarios/me`                 | Autenticado      | Retorna o perfil do usuário logado  |
| GET    | `/api/usuarios/{id}`               | ADMIN ou o próprio| Busca um usuário específico        |
| PUT    | `/api/usuarios/{id}`               | ADMIN ou o próprio| Atualiza nome/telefone             |
| PATCH  | `/api/usuarios/{id}/role`          | ADMIN            | Altera o perfil de acesso do usuário|
| DELETE | `/api/usuarios/{id}`               | ADMIN            | Exclui um usuário                   |

## Testes

```bash
# Backend
cd backend && mvn test

# Frontend
cd frontend && npx ng test --watch=false --browsers=ChromeHeadless
```

## Observações e simplificações conscientes

Por se tratar de um projeto de portfólio (sem infraestrutura de e-mail/SMS real), o fluxo de recuperação de senha **gera o token e o registra no log da aplicação** em vez de enviá-lo por e-mail/SMS. Em um ambiente de produção, esse token seria entregue ao usuário por um provedor de e-mail (ex: SES, SendGrid) ou SMS.
