# Roadmap de Evolução — Budgeting API

Registro técnico das melhorias implementadas e backlog evolutivo da aplicação.

## 1. Escopo Evolutivo

### Fase 1 — Funcionalidades e Domínio (Concluído)
- [x] **Resumo financeiro (`GetFinancialSummaryUseCase`)**:
  - Caso de uso anotado com `@Tool(name = "get-financial-summary")` para consolidação geral e por categoria.
  - Exposição via REST (`GET /transactions/summary`).
- [x] **Validação de invariantes de domínio**:
  - Validação no construtor de `Transaction` (`amount > 0`, descrição obrigatória e categoria não nula).
- [x] **Refinamento do System Prompt (`src/main/resources/prompts/system-message.st`)**:
  - Diretrizes objetivas de categorização, uso da tool de resumo e adequação textual para TTS.
- [x] **Suíte de testes unitários**:
  - Cobertura de domínio, casos de uso e controller (`TransactionTest`, `FinancialUseCasesTest`, `TransactionControllerTest`) desacoplada da API da OpenAI.

### Fase 2 — Operações Adicionais (Backlog)
- [ ] **Filtro por intervalo temporal**:
  - Inclusão de timestamp em `Transaction` e consulta entre `startDate` e `endDate` via REST e Tool Calling.
- [ ] **Exclusão de transações (`DeleteTransactionUseCase`)**:
  - Remoção por ID via `DELETE /transactions/{id}` e integração via `@Tool`.

### Fase 3 — Produção e Governança (Backlog)
- [ ] **Alertas de limite orçamentário**:
  - Definição de tetos por categoria com notificação na resposta gerada pelo assistente.
- [ ] **Auditoria de interações**:
  - Persistência do texto transcrito, tool invocada e resposta sintetizada para rastreabilidade.
- [ ] **Autenticação e isolamento de dados**:
  - Proteção dos endpoints via Spring Security / JWT e vínculo de transações por usuário.

---

## 2. Checklist de Validação

- [x] Execução local (`./gradlew bootRun`) integrada ao MySQL via `compose.yml`.
- [x] Melhorias funcionais e testes unitários implementados.
- [x] Especificação técnica e exemplos `curl` documentados no `README.md`.
- [ ] Repositório publicado no GitHub e submetido na plataforma da DIO.
