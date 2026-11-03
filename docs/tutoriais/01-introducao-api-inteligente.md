# 01 — Arquitetura e Visão Geral da API

- **Escopo**: Definição do problema, fluxo multimodal e arquitetura em camadas.

---

## 1. Objetivo

Estruturar uma API de controle financeiro capaz de processar tanto requisições REST convencionais (JSON) quanto comandos de voz em linguagem natural via Spring AI, preservando o isolamento das regras de negócio.

---

## 2. Comparativo Arquitetural

| Característica | API REST Tradicional | API Multimodal com Spring AI |
| :--- | :--- | :--- |
| Formato de entrada | Payload JSON estritamente tipado | Áudio (`multipart/form-data`) e linguagem natural |
| Tratamento de intenção | Definido estaticamente pela rota HTTP | Inferido pelo LLM e roteado via Tool Calling (`@Tool`) |
|Extração de parâmetros | Deserialização direta via Jackson | Extração semântica de descrição, valor e categoria |

### Pipeline do Assistente de Orçamento
1. **Entrada**: Upload de arquivo de áudio contendo comando financeiro.
2. **Speech-to-Text (`TranscriptionModel`)**: Transcrição do áudio para texto em português.
3. **LLM + Tool Calling (`ChatClient`)**: Extração de parâmetros e invocação do caso de uso (`PersistTransactionUseCase`, `ListTransactionsByCategoryUseCase` ou `GetFinancialSummaryUseCase`).
4. **Persistência (`TransactionRepository`)**: Execução da operação no MySQL via Spring Data JPA.
5. **Text-to-Speech (`TextToSpeechModel`)**: Síntese da resposta textual em formato `audio/mp3`.

---

## 3. Organização de Pacotes

```text
src/main/java/dio/budgeting/
├── domain/           # Entidades de negócio, objetos de valor, enums e contratos de repositório
├── application/      # Casos de uso da aplicação expostos tanto via REST quanto via @Tool
└── infrastructure/   # Adaptadores HTTP (Controllers) e persistência (Spring Data JPA)
```

> **Diretriz arquitetural**: O modelo de linguagem nunca acessa a camada de persistência diretamente; toda interação ocorre exclusivamente por meio dos casos de uso da camada `application`.
