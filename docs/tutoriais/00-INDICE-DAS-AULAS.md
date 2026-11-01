# 📚 Trilha Completa Passo a Passo — Desenvolvendo sua API Inteligente com Spring AI

Este diretório contém os **12 tutoriais passo a passo** correspondentes a cada vídeo/aula do módulo **Spring AI (Budgeting API)** conduzido pelo expert Thiago Poiani na DIO, **ordenados cronologicamente e pedagogicamente** pelo conteúdo construído em aula.

---

## 🧭 Ordem Cronológica das Aulas e Vídeos

| Ordem | Tutorial (`.md`) | Vídeo Original (`instruções-java-itau-ia`) | Duração | Foco Principal |
| :---: | :--- | :--- | :---: | :--- |
| **01** | [01-introducao-api-inteligente.md](01-introducao-api-inteligente.md) | `Introdução ao desenvolvendo sua API Inteligente.mp4` | ~09:42 | Visão geral, evolução das interfaces (JSON vs Voz/NLP) e arquitetura do Assistente de Budgeting |
| **02** | [02-spring-ai-setup-integracao-llms.md](02-spring-ai-setup-integracao-llms.md) | `Spring AI Setup e Integração com LLMs.mp4` | ~14:15 | Setup do Gradle (`spring-ai-bom`), starter OpenAI e configuração da `OPENAI_API_KEY` |
| **03** | [03-explorando-chatmodel-modelos-linguagem.md](03-explorando-chatmodel-modelos-linguagem.md) | `Explorando o ChatModel e Modelos de Linguagem.mp4` | ~22:27 | Uso de `OpenAiChatModel`, `OpenAiChatOptions`, `OpenAiChatModelIT` e endpoint exploratório |
| **04** | [04-chatclient-fluencia-contexto.md](04-chatclient-fluencia-contexto.md) | `ChatClient Fluência e Contexto no Spring AI.mp4` | ~13:17 | API fluente `ChatClient`, `defaultSystem`, `OpenAiChatClientIT` e `ChatClientController` |
| **05** | [05-tool-calling-executando-funcoes-reais.md](05-tool-calling-executando-funcoes-reais.md) | `Tool Calling Executando Funções Reais com IA.mp4` | ~10:25 | Anotação `@Tool`, execução de métodos Java pelo LLM, logs `DEBUG` e `ToolCallingIT` |
| **06** | [06-transcription-api-audio-em-texto.md](06-transcription-api-audio-em-texto.md) | `Transcription API Transformando Áudio em Texto.mp4` | ~23:29 | Speech-to-Text (`whisper-1`), prompt de contexto em PT-BR e `OpenAiTranscriptionModelIT` |
| **07** | [07-speech-api-text-to-speech.md](07-speech-api-text-to-speech.md) | `Speech API Sintetizando Voz com Text to Speech.mp4` | ~16:45 | Text-to-Speech (`gpt-4o-mini-tts`, voz `nova`), geração de MP3 e `OpenAiSpeechModelIT` |
| **08** | [08-integracao-assistente-fluxo-budget.md](08-integracao-assistente-fluxo-budget.md) | `Integração do Assistente Orquestrando o Fluxo de Budget.mp4` | ~17:23 | Modelagem DDD (`domain` e `application`): `Transaction`, `Category` e `PersistTransactionUseCase` |
| **09** | [09-persistencia-infraestrutura-docker.md](09-persistencia-infraestrutura-docker.md) | `Persistência e Infraestrutura Configurando o Banco com Docker.mp4` | ~24:12 | `compose.yml` (MySQL), Spring Data JPA, `TransactionEntity` e `JpaTransactionRepository` |
| **10** | [10-exposicao-rest-transaction-controller.md](10-exposicao-rest-transaction-controller.md) | `Exposição REST Implementando o TransactionController.mp4` | ~14:06 | `ListTransactionsByCategoryUseCase`, DTOs HTTP e endpoints `POST /transactions` e `GET /transactions/{category}` |
| **11** | [11-endpoint-transcricao-audio-controller.md](11-endpoint-transcricao-audio-controller.md) | `Endpoint de Transcrição Integrando Áudio ao Controller.mp4` | ~23:29 | Registro de `@Tool` nos Use Cases, `system-message.st` e orquestração completa no `POST /transactions/ai` |
| **12** | [12-roadmap-auditoria-evoluindo-api.md](12-roadmap-auditoria-evoluindo-api.md) | `Roadmap e Auditoria Evoluindo a API Inteligente.mp4` | ~05:27 | Auditoria arquitetural, MCP Server, segurança, testes e guia de evolução para entrega do desafio |

---

## 🛠️ Como Usar estes Tutoriais

1. Siga os arquivos na numeração de **01 a 12** para entender exatamente o raciocínio construído pelo instrutor.
2. Em cada arquivo você encontrará:
   - **Objetivo da Aula** e conceitos explicados no vídeo;
   - **Passo a Passo de Implementação** com o código completo dos arquivos criados/alterados;
   - **Como Executar e Testar** a etapa atual;
   - **Como Refinar no Repositório** (boas práticas para destacar seu projeto no GitHub).
