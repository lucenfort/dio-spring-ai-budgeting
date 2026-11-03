package dio.budgeting;

import dio.budgeting.domain.Category;
import dio.budgeting.infrastructure.http.TransactionController;
import dio.budgeting.infrastructure.http.request.TransactionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.openai.api-key=sk-test-context")
class BudgetingApplicationTests {

    @Autowired
    private TransactionController transactionController;

    @Test
    void contextLoadsAndPersistsTransactionsViaJpa() {
        var created = transactionController.createTransaction(
                new TransactionRequest("Supermercado Mensal", Category.GROCERIES, 25000L));

        assertThat(created.id()).isNotBlank();
        assertThat(created.category()).isEqualTo("GROCERIES");
        assertThat(created.amount()).isEqualTo(25000.0);

        var list = transactionController.readTransactions(Category.GROCERIES);
        assertThat(list).extracting("id").contains(created.id());

        var summary = transactionController.getFinancialSummary();
        assertThat(summary.totalTransactions()).isGreaterThanOrEqualTo(1);
        assertThat(summary.totalByCategory()).containsEntry("GROCERIES", 25000.0);
    }

}
