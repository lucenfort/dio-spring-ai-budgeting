# Aula 11 — Endpoint de Transcrição: Integrando Áudio ao Controller

- **Vídeo correspondente**: `Endpoint de Transcrição Integrando Áudio ao Controller.mp4` (~23m29s)
- **Etapa do Projeto**: Criação do `system-message.st`, registro dos casos de uso como `@Tool` no `ChatClient` e implementação completa do endpoint `POST /transactions/ai`.

---

## 🎯 1. Objetivo da Aula

Conectar todas as pontas estudadas nas aulas anteriores dentro do endpoint `POST /transactions/ai` do [`TransactionController`](../../src/main/java/dio/budgeting/infrastructure/http/TransactionController.java):
1. Receber um arquivo de áudio (`MultipartFile`);
2. Transcrever o áudio em texto com `TranscriptionModel`;
3. Enviar o texto transcrito ao `ChatClient` configurado com o `system-message.st` e os casos de uso anotados com `@Tool`;
4. Converter a resposta final da IA em áudio MP3 usando `TextToSpeechModel` e devolvê-la ao cliente.

---

## 🛠️ 2. Passo a Passo de Implementação

### Passo 2.1 — Criar o Template de Prompt do Sistema (`system-message.st`)
No arquivo [`src/main/resources/prompts/system-message.st`](../../src/main/resources/prompts/system-message.st):

```text
Você é um assistente financeiro inteligente em português brasileiro.
Sua tarefa é extrair dados de transações financeiras e usar as ferramentas disponíveis para registrar gastos, listar transações por categoria ou apresentar um resumo financeiro consolidado.
Ao registrar uma transação, escolha a categoria que melhor se adapta ao contexto (GROCERIES, PHARMA ou AUTO).
Quando a pessoa usuária perguntar sobre o total gasto geral, resumo do orçamento ou visão consolidada das despesas, utilize a ferramenta de resumo financeiro (get-financial-summary).
Responda sempre de forma objetiva, natural e adequada para leitura em voz alta.
```

### Passo 2.2 — Configurar o `ChatClient` e o Endpoint `/transactions/ai` no `TransactionController`
Veja a implementação completa em [`TransactionController.java`](../../src/main/java/dio/budgeting/infrastructure/http/TransactionController.java):

```java
    public TransactionController(PersistTransactionUseCase persistTransactionUseCase,
                                 ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase,
                                 GetFinancialSummaryUseCase getFinancialSummaryUseCase,
                                 TranscriptionModel transcriptionModel,
                                 @Value("classpath:prompts/system-message.st") Resource systemPrompt,
                                 ChatClient.Builder chatClientBuilder,
                                 TextToSpeechModel textToSpeechModel) throws IOException {
        this.persistTransactionUseCase = persistTransactionUseCase;
        this.listTransactionsByCategoryUseCase = listTransactionsByCategoryUseCase;
        this.getFinancialSummaryUseCase = getFinancialSummaryUseCase;
        this.transcriptionModel = transcriptionModel;
        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt.getContentAsString(Charset.defaultCharset()))
                .defaultTools(persistTransactionUseCase, listTransactionsByCategoryUseCase, getFinancialSummaryUseCase)
                .build();
        this.textToSpeechModel = textToSpeechModel;
    }

    @PostMapping(value = "/ai", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "audio/mp3")
    ResponseEntity<Resource> transcribe(@RequestParam("file") MultipartFile file) {
        var userMessage = transcriptionModel.transcribe(file.getResource());
        var result = chatClient.prompt().user(userMessage).call().content();

        byte[] audio = textToSpeechModel.call(result);
        var resource = new ByteArrayResource(audio);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("audio.mp3")
                                .build()
                                .toString())
                .body(resource);
    }
```

---

## ▶️ 3. Como Executar e Testar o Fluxo Multimodal Completo

1. Com a aplicação em execução (`./gradlew bootRun`), envie um dos áudios de teste para o endpoint `/transactions/ai`:
   ```bash
   curl -X POST http://localhost:8080/transactions/ai \
     -F "file=@src/test/resources/audio/recording-1.m4a" \
     --output resposta-recording-1.mp3
   ```
2. Verifique no console que:
   - O áudio `recording-1.m4a` foi transcrito (*"Gastei na farmácia rapidinho e deixei 80 reais em três itens."*);
   - O `DefaultToolCallingManager` acionou a ferramenta `persist-transaction` com a categoria `PHARMA`;
   - O Hibernate executou o `insert into transaction_entity`;
   - O arquivo `resposta-recording-1.mp3` foi salvo com a confirmação em voz sintetizada!
3. Consulte via REST para confirmar o registro no banco:
   ```bash
   curl http://localhost:8080/transactions/PHARMA
   ```
