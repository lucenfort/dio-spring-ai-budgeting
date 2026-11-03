# Índice de Documentação Técnica — Módulos do Projeto

Documentação técnica sequencial referente à construção da **Budgeting AI API** com Spring Boot 4 e Spring AI.

---

## Sequência de Módulos

| Ordem | Documento | Tópico Técnico | Escopo |
| :---: | :--- | :--- | :--- |
| **01** | [01-introducao-api-inteligente.md](01-introducao-api-inteligente.md) | Arquitetura e Visão Geral | Comparativo de interfaces REST vs. Multimodal e divisão de camadas (Domain, Application, Infrastructure) |
| **02** | [02-spring-ai-setup-integracao-llms.md](02-spring-ai-setup-integracao-llms.md) | Configuração do Spring AI | Setup do Gradle (`spring-ai-bom`), starter OpenAI e configuração da `OPENAI_API_KEY` |
| **03** | [03-explorando-chatmodel-modelos-linguagem.md](03-explorando-chatmodel-modelos-linguagem.md) | ChatModel e Opções de Inferência | Uso de `OpenAiChatModel`, `OpenAiChatOptions` e testes de integração (`OpenAiChatModelIT`) |
| **04** | [04-chatclient-fluencia-contexto.md](04-chatclient-fluencia-contexto.md) | API Fluente ChatClient | Abstração `ChatClient`, `defaultSystem` e desacoplamento de provedores (`OpenAiChatClientIT`) |
| **05** | [05-tool-calling-executando-funcoes-reais.md](05-tool-calling-executando-funcoes-reais.md) | Tool Calling | Anotação `@Tool`, execução determinística de métodos Java pelo LLM e `ToolCallingIT` |
| **06** | [06-transcription-api-audio-em-texto.md](06-transcription-api-audio-em-texto.md) | Speech-to-Text (Transcription API) | Modelo `whisper-1`, parametrização de contexto em PT-BR e `OpenAiTranscriptionModelIT` |
| **07** | [07-speech-api-text-to-speech.md](07-speech-api-text-to-speech.md) | Text-to-Speech (Speech API) | Síntese de voz (`gpt-4o-mini-tts`), resposta HTTP `audio/mp3` e `OpenAiSpeechModelIT` |
| **08** | [08-integracao-assistente-fluxo-budget.md](08-integracao-assistente-fluxo-budget.md) | Domínio e Aplicação | Modelagem de `Transaction`, `Category`, validações de domínio e `PersistTransactionUseCase` |
| **09** | [09-persistencia-infraestrutura-docker.md](09-persistencia-infraestrutura-docker.md) | Persistência e Infraestrutura | Container MySQL (`compose.yml`), Spring Data JPA, `TransactionEntity` e `JpaTransactionRepository` |
| **10** | [10-exposicao-rest-transaction-controller.md](10-exposicao-rest-transaction-controller.md) | Exposição REST | `ListTransactionsByCategoryUseCase`, `GetFinancialSummaryUseCase`, DTOs HTTP e endpoints `/transactions` |
| **11** | [11-endpoint-transcricao-audio-controller.md](11-endpoint-transcricao-audio-controller.md) | Pipeline Multimodal (`POST /transactions/ai`) | Template `system-message.st`, registro de tools e integração STT -> LLM -> TTS no Controller |
| **12** | [12-roadmap-auditoria-evoluindo-api.md](12-roadmap-auditoria-evoluindo-api.md) | Auditoria e Evolução Arquitetural | Diretrizes para escala produtiva (Security, MCP Server, observabilidade) e resumo das evoluções |
