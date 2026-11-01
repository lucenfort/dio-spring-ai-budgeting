# Aula 01 — Introdução: Desenvolvendo sua API Inteligente

- **Vídeo correspondente**: `Introdução ao desenvolvendo sua API Inteligente.mp4` (~9m42s)
- **Etapa do Projeto**: Alinhamento conceitual, problema de negócio e arquitetura alvo.

---

## 🎯 1. Objetivo da Aula

Compreender a mudança de paradigma entre **APIs tradicionais** (que exigem payloads JSON estritamente formatados) e **APIs Inteligentes com Spring AI**, capazes de receber entradas naturais (como áudio de voz), extrair intenções/entidades com LLMs e acionar regras de negócio reais mantendo uma arquitetura limpa.

---

## 🧠 2. Principais Conceitos Abordados no Vídeo

### A Evolução das Interfaces
| O Padrão Atual (APIs Tradicionais) | A Nova Era (APIs Inteligentes com Spring AI) |
| :--- | :--- |
| Receptores de JSON estáticos | Ouvintes ativos de áudio e linguagem natural |
| Regras de validação rígidas na entrada | Compreensão de contexto fluida via Spring AI |
| Dependência de input perfeitamente estruturado | Extração automática de entidades e categorização |

### O Caso de Uso: Assistente de Budgeting
O objetivo do projeto final é eliminar a fricção do registro manual de despesas:
1. **A Entrada (Input sem fricção)**: O usuário envia um áudio dizendo, por exemplo: *"Gastei 80 reais na farmácia rapidinho em três itens"*.
2. **A Interpretação (Extração de Inteligência)**:
   - O áudio é convertido em texto (**Speech-to-Text / Transcription API**);
   - O modelo de linguagem analisa a frase e extrai **Descrição**, **Valor** e **Categoria** (`GROCERIES`, `PHARMA`, `AUTO`);
3. **Execução de Função Real (**Tool Calling**)**:
   - A IA invoca o caso de uso Java (`PersistTransactionUseCase` ou `ListTransactionsByCategoryUseCase`) para persistir ou consultar no banco MySQL;
4. **Resposta Falada (**Text-to-Speech / Speech API**)**:
   - A confirmação gerada pela IA é sintetizada em um arquivo `audio.mp3` devolvido ao cliente.

---

## 🛠️ 3. Passo a Passo Prático no Repositório

Nesta primeira etapa, garanta que a estrutura de pacotes do seu projeto reflita a separação em camadas (**DDD / Clean Architecture**) que será mantida até o fim do curso:

```text
src/main/java/dio/budgeting/
├── domain/           # Entidades puras de negócio, Enums e interfaces de repositório
├── application/      # Casos de uso da aplicação (que também serão expostos como @Tool para a IA)
└── infrastructure/   # Adaptadores HTTP (Controllers REST) e Persistência (Spring Data JPA / MySQL)
```

---

## ✅ 4. Checklist e Refinamento para o GitHub

- [x] Documentar no `README.md` o fluxo completo do **Assistente de Budgeting** (Áudio -> Transcrição -> LLM + Tool Calling -> Banco de Dados -> Áudio MP3).
- [x] Manter a regra de ouro destacada na aula: **a IA nunca acessa o banco diretamente**; ela aciona casos de uso bem definidos na camada `application`.
