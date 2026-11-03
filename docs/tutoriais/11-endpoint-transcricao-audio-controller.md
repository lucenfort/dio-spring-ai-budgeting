# 11 — Pipeline Multimodal: Endpoint de Áudio e IA (`POST /transactions/ai`)

- **Escopo**: Definição do `system-message.st`, registro de tools no `ChatClient` e implementação do endpoint `POST /transactions/ai`.

---

## 1. Objetivo

Integrar `TranscriptionModel` (STT), `ChatClient` (LLM + Tool Calling) e `TextToSpeechModel` (TTS) no endpoint `POST /transactions/ai` do [`TransactionController`](../../src/main/java/dio/budgeting/infrastructure/http/TransactionController.java).

---

## 2. Implementação

### 2.1. System Prompt (`system-message.st`)
Arquivo [`src/main/resources/prompts/system-message.st`](../../src/main/resources/prompts/system-message.st):

```text
Você é um assistente financeiro inteligente em português brasileiro.
Sua tarefa é extrair dados de transações financeiras e usar as ferramentas disponíveis para registrar gastos, listar transações por categoria ou apresentar um resumo financeiro consolidado.
Ao registrar uma transação, escolha a categoria que melhor se adapta ao contexto (GROCERIES, PHARMA ou AUTO).
Quando a pessoa usuária perguntar sobre o total gasto geral, resumo do orçamento ou visão consolidada das despesas, utilize a ferramenta de resumo financeiro (get-financial-summary).
Responda sempre de forma objetiva, natural e adequada para leitura em voz alta.
```

### 2.2. Orquestração no `TransactionController`
Em [`TransactionController.java`](../../src/main/java/dio/budgeting/infrastructure/http/TransactionController.java):

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
                    ContentDisposition.attachment().filename("audio.mp3").build().toString())
            .body(resource);
}
```

---

## 3. Validação

```bash
curl -X POST http://localhost:8080/transactions/ai \
  -F "file=@src/test/resources/audio/recording-1.m4a" \
  --output resposta-recording-1.mp3
```
