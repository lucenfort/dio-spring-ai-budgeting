# Guia de Validação Local, Publicação e Entrega

Procedimento técnico para configuração da credencial da OpenAI, validação da aplicação, publicação no GitHub e submissão do desafio.

---

## 1. Configuração da Chave de API da OpenAI (`OPENAI_API_KEY`)

Para executar os testes de integração (`*IT`) e o endpoint multimodal `POST /transactions/ai` aplicando o princípio do menor privilégio, configure uma **Project API Key** no painel da OpenAI (`https://platform.openai.com/api-keys`) com as especificações abaixo.

### 1.1. Tipo e Modo de Permissão
- **Escopo**: Project Key vinculada a um projeto dedicado (ex.: `dio-budgeting-api`).
- **Owned by**: `You` (User key) ou `Service account`.
- **Permissions (Modo)**: Selecione **`Restricted`** (evite o modo `All` para chaves de desenvolvimento/teste).

### 1.2. Permissões na Interface da OpenAI (Modo `Restricted`)
Com a aba **`Restricted`** selecionada, mantenha todas as opções desmarcadas (`None`) e habilite apenas:

| Item na Lista da OpenAI | Permissão a Selecionar | Finalidade no Projeto |
| :--- | :---: | :--- |
| **Model capabilities** | **`Write`** | Habilita `/v1/chat/completions` (`gpt-4o-mini` + Tool Calling), `/v1/audio/transcriptions` (`whisper-1`) e `/v1/audio/speech` (`gpt-4o-mini-tts`) |
| **List models** *(opcional)* | **`Read`** | Apenas se desejar consultar `/v1/models` para diagnóstico de conectividade via `curl` |
| **Demais itens** (*Agents, Traces, Vaults, Voices, Threads, Evals, Fine-tuning, Files, Videos, Vector Stores, Prompts, Batch, Tunnels, Datasets, Safety, Project safety alerts*) | **`None`** (Desmarcado) | Não utilizados pela aplicação (`Voices` refere-se a vozes customizadas; a voz nativa `nova` opera via `Model capabilities`) |

### 1.3. Restrição de Modelos e Limite de Gastos (No Projeto da OpenAI)
Em **Settings -> Project -> Limits** no console da OpenAI:
1. **Model usage (Allowlist)**: Caso restrinja os modelos permitidos no projeto, habilite apenas:
   - `gpt-4o-mini`
   - `whisper-1`
   - `gpt-4o-mini-tts`
2. **Usage limits (Budget)**: Configure um limite mensal rígido (*Hard limit*, ex.: `$2.00` a `$5.00`). O custo de execução completa da suíte de testes e validações manuais deste projeto é inferior a `$0.05`.
3. **Créditos Pré-pagos (Billing)**: A API da OpenAI exige saldo ativo em **Settings -> Billing** (o plano ChatGPT Plus não inclui cota da API; sem saldo, a API retorna `429 insufficient_quota`).

### 1.4. Injeção da Chave no Ambiente Local
A chave deve ficar armazenada exclusivamente no arquivo `.env` na raiz do projeto (ignorado pelo `.gitignore` e com permissão restrita `600`). Nunca edite o arquivo `.env.example` com uma chave real, pois ele é rastreado pelo Git.

```bash
cp .env.example .env
chmod 600 .env
# Edite o arquivo .env com sua chave: OPENAI_API_KEY=sk-proj-...
```

- O `application.properties` importa automaticamente o arquivo `.env` (`spring.config.import=optional:file:.env[.properties]`) ao executar `./gradlew bootRun`.
- Por economia de tokens, `./gradlew test` ignora o `.env` por padrão (executando apenas testes unitários a custo zero). Para executar os testes de integração com a OpenAI (`*IT`), passe a flag `-PwithOpenAi`:
  ```bash
  ./gradlew test -PwithOpenAi
  ```

---

## 2. Validação Automatizada (Testes Unitários)

Compila o projeto com JDK 25 e executa a suíte de testes unitários (regras de domínio, casos de uso e endpoints REST/IA com mocks), sem necessidade de banco de dados ativo ou `OPENAI_API_KEY`:

```bash
cd /home/lucenfort/Workspace/dio-spring-ai-budgeting
./gradlew test --rerun-tasks
```

- Os testes de integração (`*IT`) são ignorados automaticamente quando `OPENAI_API_KEY` não está definida no ambiente.
- Relatório HTML gerado em `build/reports/tests/test/index.html`.

---

## 3. Validação Integrada (MySQL + API REST + OpenAI)

### 3.1. Iniciar o Banco de Dados
```bash
cd /home/lucenfort/Workspace/dio-spring-ai-budgeting
podman compose up -d
```

### 3.2. Iniciar a Aplicação
```bash
export OPENAI_API_KEY="sk-proj-..."
./gradlew bootRun
```
*(Para testar exclusivamente os endpoints REST sem consumir a API da OpenAI, defina um valor placeholder como `export OPENAI_API_KEY="sk-local-test"`).*

### 3.3. Testar Endpoints REST

1. **Criação de transações (`POST /transactions`)**:
   ```bash
   curl -i -X POST http://localhost:8080/transactions \
     -H "Content-Type: application/json" \
     -d '{"description": "Compras no supermercado", "category": "GROCERIES", "amount": 15000}'

   curl -i -X POST http://localhost:8080/transactions \
     -H "Content-Type: application/json" \
     -d '{"description": "Medicamentos", "category": "PHARMA", "amount": 8000}'
   ```

2. **Validação de domínio (`400 Bad Request`)**:
   ```bash
   curl -i -X POST http://localhost:8080/transactions \
     -H "Content-Type: application/json" \
     -d '{"description": "", "category": "AUTO", "amount": 0}'
   ```

3. **Consulta por categoria (`GET /transactions/{category}`)**:
   ```bash
   curl -s http://localhost:8080/transactions/GROCERIES
   ```

4. **Resumo financeiro consolidado (`GET /transactions/summary`)**:
   ```bash
   curl -s http://localhost:8080/transactions/summary
   ```

### 3.4. Testar Pipeline de Voz (`POST /transactions/ai`)
Requer `OPENAI_API_KEY` válida:
```bash
curl -X POST http://localhost:8080/transactions/ai \
  -F "file=@src/test/resources/audio/recording-1.m4a" \
  --output resposta-assistente.mp3
```

Para encerrar o ambiente após os testes:
```bash
podman compose down
```

---

## 4. Publicação no GitHub

Com o GitHub CLI (`gh`) autenticado:

```bash
cd /home/lucenfort/Workspace/dio-spring-ai-budgeting

gh repo create dio-spring-ai-budgeting \
  --public \
  --description "Budgeting REST & Multimodal Voice API with Spring Boot 4, Spring AI and Java 25" \
  --source=. \
  --remote=origin \
  --push
```

---

## 5. Submissão na Plataforma da DIO

- **URL do Repositório**: `https://github.com/lucenfort/dio-spring-ai-budgeting`
- **Resumo Técnico da Entrega**:
  > API de controle de orçamento com Spring Boot 4, Java 25 e Spring AI (ChatClient, Tool Calling, Whisper STT e TTS). Evoluções implementadas: (1) Caso de uso `GetFinancialSummaryUseCase` exposto via `@Tool("get-financial-summary")` e endpoint `GET /transactions/summary`; (2) Validações de domínio na entidade `Transaction` com tratamento `400 Bad Request`; (3) Refinamento do System Prompt em português; (4) Testes unitários automatizados (`TransactionTest`, `FinancialUseCasesTest`, `TransactionControllerTest`) integrados ao GitHub Actions CI.
