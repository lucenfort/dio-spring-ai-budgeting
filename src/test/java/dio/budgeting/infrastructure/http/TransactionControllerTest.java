package dio.budgeting.infrastructure.http;

import dio.budgeting.application.GetFinancialSummaryUseCase;
import dio.budgeting.application.ListTransactionsByCategoryUseCase;
import dio.budgeting.application.PersistTransactionUseCase;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.infrastructure.http.request.TransactionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransactionControllerTest {

    private TransactionController controller;
    private TranscriptionModel transcriptionModel;
    private TextToSpeechModel textToSpeechModel;
    private ChatClient chatClient;

    @BeforeEach
    void setUp() throws IOException {
        var repo = new InMemoryRepo();
        var persistUseCase = new PersistTransactionUseCase(repo);
        var listUseCase = new ListTransactionsByCategoryUseCase(repo);
        var summaryUseCase = new GetFinancialSummaryUseCase(repo);

        transcriptionModel = mock(TranscriptionModel.class);
        textToSpeechModel = mock(TextToSpeechModel.class);
        var chatClientBuilder = mock(ChatClient.Builder.class, RETURNS_DEEP_STUBS);
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);

        when(chatClientBuilder.defaultSystem(anyString()).defaultTools(any(), any(), any()).build())
                .thenReturn(chatClient);

        controller = new TransactionController(
                persistUseCase,
                listUseCase,
                summaryUseCase,
                transcriptionModel,
                new ClassPathResource("prompts/system-message.st"),
                chatClientBuilder,
                textToSpeechModel
        );
    }

    @Test
    void shouldCreateListAndSummarizeTransactionsViaController() {
        var created = controller.createTransaction(new TransactionRequest("Farmácia", Category.PHARMA, 8000L));
        assertThat(created.id()).isNotBlank();
        assertThat(created.category()).isEqualTo("PHARMA");
        assertThat(created.amount()).isEqualTo(8000.0);

        var pharmaList = controller.readTransactions(Category.PHARMA);
        assertThat(pharmaList).hasSize(1);

        var summary = controller.getFinancialSummary();
        assertThat(summary.totalTransactions()).isEqualTo(1);
        assertThat(summary.totalAmount()).isEqualTo(8000.0);
        assertThat(summary.totalByCategory()).containsEntry("PHARMA", 8000.0);
    }

    @Test
    void shouldReturnBadRequestMapWhenValidationFails() {
        assertThatThrownBy(() -> controller.createTransaction(new TransactionRequest("", Category.AUTO, 0L)))
                .isInstanceOf(IllegalArgumentException.class);

        var errorResponse = controller.handleIllegalArgumentException(
                new IllegalArgumentException("O valor da transação deve ser maior que zero."));
        assertThat(errorResponse).containsEntry("error", "O valor da transação deve ser maior que zero.");
    }

    @Test
    void shouldProcessAudioAndReturnSynthesizedMp3Response() {
        var mockAudio = new MockMultipartFile("file", "recording-1.m4a", "audio/m4a", new byte[]{1, 2, 3});
        byte[] expectedMp3 = new byte[]{10, 20, 30, 40};

        when(transcriptionModel.transcribe(any())).thenReturn("Gastei 80 reais na farmácia");
        when(chatClient.prompt().user("Gastei 80 reais na farmácia").call().content())
                .thenReturn("Despesa de 80 reais na categoria farmácia registrada com sucesso.");
        when(textToSpeechModel.call("Despesa de 80 reais na categoria farmácia registrada com sucesso."))
                .thenReturn(expectedMp3);

        var response = controller.transcribe(mockAudio);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("audio.mp3");
        assertThat(response.getBody()).isNotNull();
    }

    private static class InMemoryRepo implements TransactionRepository {
        private final List<Transaction> store = new ArrayList<>();

        @Override
        public Transaction save(Transaction transaction) {
            store.add(transaction);
            return transaction;
        }

        @Override
        public List<Transaction> findAllByCategory(Category category) {
            return store.stream().filter(t -> t.getCategory() == category).toList();
        }

        @Override
        public List<Transaction> findAll() {
            return List.copyOf(store);
        }
    }
}
