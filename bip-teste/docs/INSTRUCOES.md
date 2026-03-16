# 📋 Instruções do Desafio

## Objetivo

Criar uma solução fullstack completa em camadas corrigindo um bug crítico no módulo EJB
e entregando uma aplicação funcional com DB, EJB, Backend e Frontend.

## Tarefas

1. Executar `db/schema.sql` e `db/seed.sql` no banco PostgreSQL
2. Corrigir o bug no `BeneficioEjbService` (ver seção Bug abaixo)
3. Implementar o backend Spring Boot com CRUD + integração com EJB
4. Desenvolver o frontend Angular consumindo o backend
5. Implementar testes (unitários e integração)
6. Documentar com Swagger e README
7. Submeter via repositório próprio

## Bug no EJB

O método de transferência original apresenta os seguintes problemas:

- Não verifica saldo antes de transferir (pode gerar saldo negativo)
- Não usa locking (race condition em acessos concorrentes)
- Não declara transação explicitamente
- Não valida entradas

**Correção esperada:** validações completas, verificação de saldo,
`PESSIMISTIC_WRITE` com anti-deadlock e `@TransactionAttribute(REQUIRED)`.

## Estrutura esperada

```
bip-teste-integrado/
├── db/                    # schema.sql + seed.sql
├── ejb-module/            # EJB com bug corrigido
├── backend-module/        # Spring Boot CRUD + integração
├── frontend/              # Angular app
├── docs/                  # Este arquivo + critérios
└── .github/workflows/     # CI pipeline
```
