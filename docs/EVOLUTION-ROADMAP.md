# Roadmap de Evolução — Desafio DIO Spring AI (Budgeting API)

Este documento reúne sugestões práticas e estruturadas para evoluir o projeto base e destacar sua entrega no desafio da DIO.

## 1. Ideias de Evolução por Nível

### Nível 1 — Melhorias Rápidas e Diretas (Recomendado para começar)
- [x] **Listar todas as transações / Resumo financeiro (`GetFinancialSummaryUseCase`)**:
  - Criar um caso de uso anotado com `@Tool` para retornar o total gasto geral ou por categoria.
  - Expor no `TransactionController` (`GET /transactions/summary`).
- [x] **Validação de Domínio**:
  - Validar no construtor de `Transaction` se o valor (`amount`) é maior que zero, se a descrição não está em branco e se a categoria foi informada.
- [x] **Melhoria do System Prompt (`src/main/resources/prompts/system-message.st`)**:
  - Ajustar o tom de voz do assistente, incluir suporte à ferramenta de resumo financeiro e orientar respostas naturais para leitura em voz alta.

### Nível 2 — Evoluções Intermediárias
- [ ] **Filtro por Período de Datas**:
  - Consultar transações entre `startDate` e `endDate` tanto via endpoint REST quanto via Tool Calling da IA.
- [ ] **Exclusão ou Estorno de Transação (`DeleteTransactionUseCase`)**:
  - Permitir comandos de voz como *"Apague o último gasto que registrei"* ou exclusão por ID via REST (`DELETE /transactions/{id}`).
- [x] **Testes Unitários dos Casos de Uso**:
  - Criar testes unitários rápidos (`TransactionTest` e `FinancialUseCasesTest`) que rodem sem depender de chave da OpenAI.

### Nível 3 — Evoluções Avançadas
- [ ] **Limite de Orçamento por Categoria (Budget Alert)**:
  - Avisar na resposta de voz quando o gasto acumulado em uma categoria ultrapassar uma meta definida.
- [ ] **Auditoria de Comandos de Voz**:
  - Salvar o histórico do texto transcrito e da resposta gerada pela IA para fins de auditoria.

---

## 2. Checklist de Entrega para a DIO

- [ ] Projeto executando localmente (`./gradlew bootRun`) com banco MySQL via Docker Compose (`compose.yml`).
- [ ] Pelo menos **uma melhoria funcional** implementada e testada.
- [ ] Seção **"Qual melhoria foi implementada"** preenchida no `README.md` principal.
- [ ] Exemplos de requisições (`curl` / Postman / Insomnia) documentados no `README.md`.
- [ ] Repositório publicado no GitHub e link enviado na plataforma da DIO.
