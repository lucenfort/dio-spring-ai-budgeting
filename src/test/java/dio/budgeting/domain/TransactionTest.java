package dio.budgeting.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionTest {

    @Test
    void shouldCreateValidTransaction() {
        var transaction = new Transaction("Compras no mercado", 15000L, Category.GROCERIES);

        assertThat(transaction.getId()).isNotNull();
        assertThat(transaction.getDescription()).isEqualTo("Compras no mercado");
        assertThat(transaction.getAmount()).isEqualTo(15000L);
        assertThat(transaction.getCategory()).isEqualTo(Category.GROCERIES);
    }

    @Test
    void shouldRejectBlankDescription() {
        assertThatThrownBy(() -> new Transaction("   ", 1000L, Category.PHARMA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A descrição da transação é obrigatória.");
    }

    @Test
    void shouldRejectNonPositiveAmount() {
        assertThatThrownBy(() -> new Transaction("Remédio", 0L, Category.PHARMA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O valor da transação deve ser maior que zero.");
    }

    @Test
    void shouldRejectNullCategory() {
        assertThatThrownBy(() -> new Transaction("Combustível", 5000L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A categoria da transação é obrigatória.");
    }
}
