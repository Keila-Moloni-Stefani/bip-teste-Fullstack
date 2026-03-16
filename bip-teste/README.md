# BIP – Desafio Fullstack Integrado

Desafio técnico BIP com arquitetura completa em camadas: DB, EJB, Backend e Frontend, com foco em
correção do bug transacional, qualidade de código, cobertura de testes e entrega de aplicação funcional.

---

## Arquitetura

```
┌──────────────────────────────────────────────────────────────────────┐
│                    Frontend Angular 17  (porta 4200)                 │
│  BeneficioListComponent │ BeneficioFormComponent │ Transferencia     │
│  BeneficioService (HTTP) │ Reactive Forms │ Tipagem com interfaces   │
└─────────────────────────────┬────────────────────────────────────────┘
                              │ HTTP REST (/api/v1)
┌─────────────────────────────▼────────────────────────────────────────┐
│              Backend Spring Boot 3.2  (porta 8081)                   │
│  BeneficioController (@Valid, Swagger)                               │
│  BeneficioService (@Transactional)   ──► RestTemplate ──► EJB       │
│  BeneficioRepository (JPA)                                           │
│  GlobalExceptionHandler (@RestControllerAdvice)                      │
│  DTOs separados da entidade JPA                                      │
└──────────────┬──────────────────────────┬────────────────────────────┘
               │ JPA / PostgreSQL         │ HTTP REST
               ▼                          ▼
┌──────────────────────┐    ┌─────────────────────────────────────────┐
│  PostgreSQL (5432)   │    │    EJB Module – WildFly  (porta 8080)   │
│  Tabela: beneficio   │    │    BeneficioEjbService                  │
│  (versao = OCC)      │    │    @TransactionAttribute(REQUIRED)      │
└──────────────────────┘    │    PESSIMISTIC_WRITE + anti-deadlock    │
                            └─────────────────────────────────────────┘
```

---

## Bug EJB Corrigido

O `BeneficioEjbService` original apresentava **4 problemas críticos**:

| # | Bug Original | Correção Aplicada |
|---|---|---|
| 1 | Sem validação de entrada | Validações de nulidade, valor positivo e IDs distintos |
| 2 | Sem verificação de saldo | `IllegalStateException` com mensagem detalhada |
| 3 | Sem locking — race condition / lost update | `PESSIMISTIC_WRITE` com lock em ordem crescente de ID (anti-deadlock) |
| 4 | Sem demarcação de transação | `@TransactionAttribute(REQUIRED)` + rollback automático |

---

## Como Executar

### Opção 1 — Docker Compose (recomendado)

```bash
docker-compose up --build
```

Serviços disponíveis após o startup:

| Serviço | URL |
|---|---|
| Frontend Angular | http://localhost:4200 |
| Backend API | http://localhost:8081 |
| Swagger UI | http://localhost:8081/swagger-ui.html |
| PostgreSQL | localhost:5432 |
| WildFly Admin | http://localhost:9990 |

### Opção 2 — Local

**Backend:**
```bash
cd backend-module
mvn spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npm start   # http://localhost:4200
```

---

## 📋 Endpoints da API

| Método | Endpoint | Descrição |
|--------|---|---|
| `GET` | `/api/v1/beneficios` | Lista todos |
| `GET` | `/api/v1/beneficios?apenasAtivos=true` | Filtra apenas ativos |
| `GET` | `/api/v1/beneficios/{id}` | Busca por ID |
| `POST` | `/api/v1/beneficios` | Cria novo |
| `PUT` | `/api/v1/beneficios/{id}` | Atualiza |
| `DELETE` | `/api/v1/beneficios/{id}` | Remove |
| `POST` | `/api/v1/beneficios/transferencia` | Transfere via EJB |

**Exemplos de payload:**

```json
// Criar / atualizar
{ "nome": "Benefício X", "descricao": "Descrição", "valor": 1500.00, "ativo": true }

// Transferência
{ "fromId": 1, "toId": 2, "amount": 300.00 }
```

---

## Estrutura do Projeto

```
bip-ideal/
├── .github/workflows/
│   └── ci.yml                          # CI: 3 jobs paralelos
├── db/
│   ├── schema.sql                      # DDL com IF NOT EXISTS
│   └── seed.sql                        # 4 registros iniciais
├── ejb-module/
│   └── src/main/java/com/example/ejb/
│       ├── Beneficio.java              # Entidade JPA + @Version
│       ├── BeneficioEjbService.java    # Bug corrigido (4 fixes)
│       └── JAXRSConfiguration.java
├── backend-module/
│   └── src/
│       ├── main/java/com/example/backend/
│       │   ├── config/OpenApiConfig.java
│       │   ├── controller/BeneficioController.java  # @Valid + Swagger
│       │   ├── dto/                                 # DTO separado da entidade
│       │   │   ├── BeneficioDTO.java
│       │   │   ├── BeneficioRequest.java            # Bean Validation
│       │   │   └── TransferenciaRequest.java
│       │   ├── entity/Beneficio.java
│       │   ├── exception/
│       │   │   ├── GlobalExceptionHandler.java      # @RestControllerAdvice
│       │   │   ├── ResourceNotFoundException.java
│       │   │   └── BusinessException.java
│       │   ├── repository/BeneficioRepository.java  # findByAtivo()
│       │   └── service/BeneficioService.java        # @Transactional
│       └── test/java/com/example/backend/
│           ├── BeneficioServiceTest.java            # 10 testes unitários
│           └── BeneficioControllerIntegrationTest.java  # 8 testes MockMvc
├── frontend/src/app/
│   ├── app.component.ts                # Navbar + router-outlet
│   ├── app.config.ts
│   ├── app.routes.ts                   # Lazy loading
│   ├── components/
│   │   ├── beneficio-list/             # Tabela + filtro + CRUD
│   │   ├── beneficio-form/             # Criar / editar
│   │   └── transferencia/             # Saldo em tempo real
│   ├── models/beneficio.model.ts       # Interfaces TypeScript
│   └── services/
│       ├── beneficio.service.ts
│       └── beneficio.service.spec.ts   # 8 testes HTTP
└── docker-compose.yml
```

---

## Checklist de Tarefas

- [x] Scripts `db/schema.sql` e `db/seed.sql`
- [x] **Bug EJB corrigido** — validação, saldo, `PESSIMISTIC_WRITE`, `@TransactionAttribute(REQUIRED)`
- [x] Backend CRUD completo com DTOs separados
- [x] `@Valid` nos endpoints + `GlobalExceptionHandler`
- [x] Endpoint `/transferencia` integrado com EJB
- [x] Testes unitários do serviço (10 — Mockito)
- [x] Testes de integração do controller (8 — MockMvc)
- [x] Testes do serviço Angular (8 — Jasmine/HttpTestingController)
- [x] Swagger UI (SpringDoc OpenAPI)
- [x] Frontend Angular 17 com listagem, CRUD e transferência
- [x] Filtro "apenas ativos" no frontend e backend
- [x] Exibição de saldo disponível na transferência
- [x] Lazy loading de rotas no Angular
- [x] Pipeline CI com 3 jobs paralelos + cache + upload de artefatos
- [x] Docker Compose com healthcheck no banco


---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Banco | PostgreSQL 16 |
| EJB | Jakarta EE 10, WildFly |
| Backend | Java 17, Spring Boot 3.2, Spring Data JPA, SpringDoc OpenAPI |
| Frontend | Angular 17 (standalone), TypeScript 5.2, Reactive Forms |
| Testes | JUnit 5, Mockito, MockMvc (backend) · Jasmine, Karma (Angular) |
| CI | GitHub Actions (3 jobs paralelos) |
| Infra | Docker, Docker Compose |

---

Teste Prático Fullstack Integrado

Desenvolvido por Keila Moloni Stefani
