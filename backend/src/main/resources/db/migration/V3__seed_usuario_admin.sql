-- Usuario administrador padrao para uso em ambiente de desenvolvimento/demonstracao.
-- Login: (11) 99999-9999  |  Senha: Admin@123
-- Trocar a senha em qualquer ambiente que nao seja local.
INSERT INTO usuarios (nome, telefone, senha, role, ativo, criado_em, atualizado_em)
VALUES (
    'Administrador',
    '(11) 99999-9999',
    '$2b$12$a5NoZjPETkOI/IhBiWXQoeUJF3KQIyJbowtHFGyHoC11Vv9cAzXAC',
    'ADMIN',
    TRUE,
    NOW(),
    NOW()
);
