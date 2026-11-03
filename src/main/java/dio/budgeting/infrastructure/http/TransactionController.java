package dio.budgeting.infrastructure.http;

import dio.budgeting.application.GetFinancialSummaryUseCase;
import dio.budgeting.application.ListTransactionsByCategoryUseCase;
import dio.budgeting.application.PersistTransactionUseCase;
import dio.budgeting.application.output.FinancialSummaryOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.infrastructure.http.request.TransactionRequest;
import dio.budgeting.infrastructure.http.response.TransactionResponse;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    static final long MAX_AUDIO_SIZE_BYTES = 1_048_576L; // 1 MB
    static final int MAX_TRANSCRIPTION_LENGTH = 500;
    static final int MAX_TTS_LENGTH = 400;
    private static final Set<String> SUPPORTED_AUDIO_EXTENSIONS = Set.of(
            ".m4a", ".mp3", ".wav", ".ogg", ".webm", ".mp4", ".mpeg", ".mpga", ".flac"
    );

    private final PersistTransactionUseCase persistTransactionUseCase;
    private final ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase;
    private final GetFinancialSummaryUseCase getFinancialSummaryUseCase;

    private final TranscriptionModel transcriptionModel;
    private final ChatClient chatClient;
    private final TextToSpeechModel textToSpeechModel;

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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransaction(@RequestBody TransactionRequest request) {
        var transaction = persistTransactionUseCase.execute(request.toInput());
        return TransactionResponse.from(transaction);
    }

    @GetMapping("/summary")
    public FinancialSummaryOutput getFinancialSummary() {
        return getFinancialSummaryUseCase.execute();
    }

    @GetMapping("/{category}")
    public List<TransactionResponse> readTransactions(@PathVariable Category category) {
        return listTransactionsByCategoryUseCase.execute(category).stream().map(TransactionResponse::from).toList();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }

    @PostMapping(value = "/ai", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "audio/mp3")
    ResponseEntity<Resource> transcribe(@RequestParam("file") MultipartFile file) {
        validateAudioFile(file);

        var userMessage = transcriptionModel.transcribe(file.getResource());
        if (userMessage == null || userMessage.isBlank()) {
            throw new IllegalArgumentException("Não foi possível identificar fala no áudio enviado.");
        }
        if (userMessage.length() > MAX_TRANSCRIPTION_LENGTH) {
            throw new IllegalArgumentException("O comando de voz excede o limite máximo de caracteres permitido.");
        }

        var result = chatClient.prompt().user(userMessage.trim()).call().content();
        var safeTtsText = sanitizeForSpeech(result);

        byte[] audio = textToSpeechModel.call(safeTtsText);
        var resource = new ByteArrayResource(audio);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("audio.mp3")
                                .build()
                                .toString())
                .body(resource);
    }

    private void validateAudioFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("O arquivo de áudio é obrigatório e não pode estar vazio.");
        }
        if (file.getSize() > MAX_AUDIO_SIZE_BYTES) {
            throw new IllegalArgumentException("O arquivo de áudio excede o tamanho máximo permitido de 1 MB.");
        }
        if (!isSupportedAudioFormat(file)) {
            throw new IllegalArgumentException("Formato de arquivo inválido. Envie um arquivo de áudio suportado.");
        }
    }

    private boolean isSupportedAudioFormat(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("audio/")) {
            return true;
        }
        String filename = file.getOriginalFilename();
        if (filename != null) {
            String lower = filename.toLowerCase(Locale.ROOT);
            return SUPPORTED_AUDIO_EXTENSIONS.stream().anyMatch(lower::endsWith);
        }
        return false;
    }

    private String sanitizeForSpeech(String text) {
        if (text == null || text.isBlank()) {
            return "Operação concluída.";
        }
        String normalized = text.trim();
        if (normalized.length() > MAX_TTS_LENGTH) {
            return normalized.substring(0, MAX_TTS_LENGTH);
        }
        return normalized;
    }
}
