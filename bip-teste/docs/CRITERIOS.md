# 📊 Critérios de Avaliação

| Critério | Peso | O que é avaliado |
|---|---|---|
| Arquitetura em camadas | 20% | Separação clara: DB → EJB → Backend → Frontend. DTOs, repositórios, serviços e controllers bem definidos. |
| Correção EJB | 20% | Validação de entrada, verificação de saldo, locking (pessimista ou otimista), controle de transação. |
| CRUD + Transferência | 15% | Endpoints GET, POST, PUT, DELETE funcionando. Endpoint de transferência integrado com EJB. |
| Qualidade de código | 10% | Nomenclatura, organização de pacotes, sem código duplicado, boas práticas de Java/TypeScript. |
| Testes | 15% | Testes unitários do serviço (Mockito) e de integração do controller (MockMvc). Testes Angular opcionais. |
| Documentação | 10% | Swagger/OpenAPI disponível. README com instruções claras de como executar. |
| Frontend | 10% | SPA Angular funcional consumindo o backend. CRUD e transferência operacionais na UI. |

## Pontuação máxima: 100 pontos

## Observações

- A correção do EJB é o critério mais importante junto com a arquitetura (40% combinados)
- Testes sem assertions válidos não pontuam
- Frontend sem integração real com o backend não pontua
- Documentação precisa ser funcional (Swagger deve abrir e listar endpoints)
