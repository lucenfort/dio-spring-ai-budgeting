package dio.budgeting.application;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FinancialUseCasesTest {

    @Test
    void shouldPersistListByCategoryAndSummarizeTransactions() {
        var inMemoryRepo = new InMemoryTransactionRepository();
        var persistUseCase = new PersistTransactionUseCase(inMemoryRepo);
        var listByCategoryUseCase = new ListTransactionsByCategoryUseCase(inMemoryRepo);
        var summaryUseCase = new GetFinancialSummaryUseCase(inMemoryRepo);

        persistUseCase.execute(new PersistTransactionInput("Supermercado", 10000L, Category.GROCERIES));
        persistUseCase.execute(new PersistTransactionInput("Padaria", 5000L, Category.GROCERIES));
        persistUseCase.execute(new PersistTransactionInput("Farmácia", 3000L, Category.PHARMA));

        var groceries = listByCategoryUseCase.execute(Category.GROCERIES);
        assertThat(groceries).hasSize(2);

        var summary = summaryUseCase.execute();

        assertThat(summary.totalTransactions()).isEqualTo(3);
        assertThat(summary.totalAmount()).isEqualTo(18000.0);
        assertThat(summary.totalByCategory())
                .containsEntry("GROCERIES", 15000.0)
                .containsEntry("PHARMA", 3000.0);
    }

    private static class InMemoryTransactionRepository implements TransactionRepository {
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
