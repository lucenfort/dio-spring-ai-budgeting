# 🚀 Instruções Finais — Validação Local, Publicação no GitHub e Entrega na DIO

Este guia reúne o passo a passo exato do que **você deve fazer manualmente** para realizar seu teste final de validação, publicar o projeto no seu GitHub e entregar o desafio na plataforma da DIO.

---

## 📌 O Que Já Está Pronto no Seu Ambiente

- **Projeto completo implementado** em `/home/lucenfort/Workspace/dio-spring-ai-budgeting`.
- **JDK 25 (`25.0.4.1`)** instalado e configurado no seu container Distrobox `fedora-dev`.
- **Gradle 9.4.1** baixado e configurado no `gradlew` (você pode rodar `./gradlew` diretamente do seu terminal normal que ele usa o JDK 25 do `fedora-dev` automaticamente) e `gradle.properties` ajustado para IPv4.
- **Wrapper `docker` -> `podman`** configurado em `~/.local/bin/docker`.
- **Suíte de testes unitários (8 testes)** criada e validada (`TransactionTest`, `FinancialUseCasesTest` e `TransactionControllerTest`).
- **Commit Git inicial** já realizado na branch `main`.

---

## 🧪 Etapa 1 — Validação Rápida (Sem Chave da OpenAI e Sem Banco)

Este teste compila todo o projeto com Java 25 e executa os **8 testes unitários** (regras de domínio, casos de uso, resumo financeiro e endpoints do `TransactionController` com mocks de áudio/IA).

Abra o terminal e execute:

```bash
cd /home/lucenfort/Workspace/dio-spring-ai-budgeting
./gradlew test --rerun-tasks
```

**Resultado esperado:**
- `BUILD SUCCESSFUL` em ~3 segundos.
- Os testes de integração que dependem da OpenAI (`*IT`) aparecerão como `SKIPPED` (pois a variável `OPENAI_API_KEY` ainda não foi definida), e todos os testes de domínio, aplicação e controller passarão (`PASSED`).
- Você também pode abrir o relatório visual no navegador:
  ```bash
  xdg-open build/reports/tests/test/index.html
  ```

---

## 🐳 Etapa 2 — Validação Completa da API Rodando (MySQL + Endpoints REST + IA)

Se você quiser subir a aplicação real com o banco de dados MySQL e testar as requisições HTTP:

### 2.1. Subir o Banco de Dados MySQL (via Podman Compose)
No terminal, dentro da pasta do projeto:

```bash
cd /home/lucenfort/Workspace/dio-spring-ai-budgeting
podman compose up -d
```

Verifique se o container do MySQL está rodando na porta `3307`:
```bash
podman ps
```

### 2.2. Iniciar a Aplicação Spring Boot
Se você **tiver uma chave da OpenAI** (`sk-...`), exporte-a antes de iniciar:
```bash
export OPENAI_API_KEY="sk-sua-chave-da-openai-aqui"
./gradlew bootRun
```
*(Observação: Caso você não tenha uma chave real da OpenAI no momento, mas queira testar apenas os endpoints REST com o banco MySQL, basta exportar um valor fictício como `export OPENAI_API_KEY="sk-teste-local"` e rodar `./gradlew bootRun`).*

### 2.3. Testar os Endpoints REST (em outro terminal)

1. **Criar transações financeiras (`POST /transactions`)**:
   ```bash
   curl -i -X POST http://localhost:8080/transactions \
     -H "Content-Type: application/json" \
     -d '{"description": "Compras do mês no supermercado", "category": "GROCERIES", "amount": 15000}'

   curl -i -X POST http://localhost:8080/transactions \
     -H "Content-Type: application/json" \
     -d '{"description": "Medicamentos na farmácia", "category": "PHARMA", "amount": 8000}'
   ```

2. **Testar a validação de domínio implementada (`400 Bad Request`)**:
   ```bash
   curl -i -X POST http://localhost:8080/transactions \
     -H "Content-Type: application/json" \
     -d '{"description": "", "category": "AUTO", "amount": 0}'
   ```

3. **Listar transações por categoria (`GET /transactions/{category}`)**:
   ```bash
   curl -s http://localhost:8080/transactions/GROCERIES
   ```

4. **Consultar o Resumo Financeiro Consolidado — Melhoria do Desafio (`GET /transactions/summary`)**:
   ```bash
   curl -s http://localhost:8080/transactions/summary
   ```
   Retorno esperado:
   ```json
   {
     "totalTransactions": 2,
     "totalAmount": 23000.0,
     "totalByCategory": {
       "GROCERIES": 15000.0,
       "PHARMA": 8000.0
     }
   }
   ```

### 2.4. Testar o Fluxo de Voz com IA (`POST /transactions/ai`) *(Requer `OPENAI_API_KEY` válida)*
Com uma chave válida da OpenAI configurada:

```bash
curl -X POST http://localhost:8080/transactions/ai \
  -F "file=@src/test/resources/audio/recording-1.m4a" \
  --output resposta-assistente.mp3
```

Depois, ouça a confirmação em voz gerada pela IA:
```bash
vlc resposta-assistente.mp3
```

Para encerrar a aplicação e o container do banco ao terminar os testes:
```bash
# No terminal do bootRun, pressione Ctrl+C e depois:
podman compose down
```

---

## 🌐 Etapa 3 — Publicar o Projeto no Seu GitHub

O seu GitHub CLI (`gh`) já está autenticado com a sua conta **`lucenfort`** e o commit já está pronto na branch `main`.

Execute os comandos abaixo para criar o repositório público no seu GitHub e fazer o push de todo o código:

```bash
cd /home/lucenfort/Workspace/dio-spring-ai-budgeting

# Cria o repositório público no seu GitHub, configura o remote 'origin' e faz o push
gh repo create dio-spring-ai-budgeting \
  --public \
  --description "API Inteligente de Controle de Orçamento (Budgeting) com Spring Boot 4, Spring AI (ChatClient, Tool Calling, Whisper STT e TTS) e Java 25 — Desafio DIO" \
  --source=. \
  --remote=origin \
  --push
```

### Validar no GitHub após o Push:
1. Acesse: `https://github.com/lucenfort/dio-spring-ai-budgeting`
2. Verifique se o `README.md` com o diagrama Mermaid está renderizando na página inicial.
3. Clique na aba **Actions** do repositório para ver o workflow **Java CI with Gradle** rodando e ficando verde ✅.

---

## 🏆 Etapa 4 — Entregar o Desafio na Plataforma da DIO

1. Acesse a página do **Desafio de Projeto (Desenvolvendo sua API Inteligente com Spring AI)** na plataforma da DIO.
2. Clique em **Entregar Projeto**.
3. No campo **URL do Repositório no GitHub**, cole:
   ```text
   https://github.com/lucenfort/dio-spring-ai-budgeting
   ```
4. No campo de **Descrição / Comentários da Entrega**, você pode usar o resumo abaixo:
   > Implementação e evolução da API Inteligente de Budgeting com Spring Boot 4, Java 25 e Spring AI (OpenAI ChatClient, Tool Calling, Whisper Transcription e Text-to-Speech).
   > Melhorias adicionadas ao projeto base:
   > 1. Nova Tool (@Tool "get-financial-summary") e caso de uso GetFinancialSummaryUseCase + endpoint REST GET /transactions/summary para consolidação de despesas por categoria e total geral.
   > 2. Validações de regras de negócio na entidade de domínio Transaction e tratamento HTTP 400 Bad Request no TransactionController.
   > 3. Evolução do System Prompt em português brasileiro e suíte de testes unitários automatizados (TransactionTest, FinancialUseCasesTest e TransactionControllerTest) integrada ao GitHub Actions CI.
5. Confirme o envio! 🎉
