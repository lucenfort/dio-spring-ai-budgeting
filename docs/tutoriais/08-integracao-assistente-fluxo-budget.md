# 08 — Camadas de Domínio e Aplicação

- **Escopo**: Implementação dos pacotes `domain` (`Category`, `TransactionId`, `Transaction`, `TransactionRepository`) e `application` (`PersistTransactionInput`, `TransactionOutput`, `PersistTransactionUseCase`).

---

## 1. Objetivo

Modelar o núcleo de negócio seguindo Domain-Driven Design (DDD) e Clean Architecture, isolando regras e invariantes na entidade `Transaction` e expondo casos de uso reutilizáveis por REST e Tool Calling.

---

## 2. Implementação

### 2.1. Camada `domain`

1. **[`Category.java`](../../src/main/java/dio/budgeting/domain/Category.java)**:
   ```java
   package dio.budgeting.domain;

   public enum Category {
       GROCERIES,
       PHARMA,
       AUTO,
   }
   ```

2. **[`TransactionId.java`](../../src/main/java/dio/budgeting/domain/TransactionId.java)**:
   ```java
   package dio.budgeting.domain;

   import java.util.UUID;

   public record TransactionId(UUID uuid) {
       public TransactionId() {
           this(UUID.randomUUID());
       }
   }
   ```

3. **[`Transaction.java`](../../src/main/java/dio/budgeting/domain/Transaction.java)**:
   ```java
   package dio.budgeting.domain;

   import lombok.AllArgsConstructor;
   import lombok.Getter;

   @Getter
   @AllArgsConstructor
   public class Transaction {
       private TransactionId id;
       private String description;
       private long amount;
       private Category category;

       public Transaction(String description, long amount, Category category) {
           if (description == null || description.isBlank()) {
               throw new IllegalArgumentException("A descrição da transação é obrigatória.");
           }
           if (amount <= 0) {
               throw new IllegalArgumentException("O valor da transação deve ser maior que zero.");
           }
           if (category == null) {
               throw new IllegalArgumentException("A categoria da transação é obrigatória.");
           }
           this.id = new TransactionId();
           this.description = description.trim();
           this.amount = amount;
           this.category = category;
       }
   }
   ```

4. **[`TransactionRepository.java`](../../src/main/java/dio/budgeting/domain/TransactionRepository.java)**:
   ```java
   package dio.budgeting.domain;

   import java.util.List;

   public interface TransactionRepository {
       Transaction save(Transaction transaction);
       List<Transaction> findAllByCategory(Category category);
       List<Transaction> findAll();
   }
   ```

### 2.2. Camada `application`

1. **[`PersistTransactionInput.java`](../../src/main/java/dio/budgeting/application/input/PersistTransactionInput.java)**:
   ```java
   package dio.budgeting.application.input;

   import dio.budgeting.domain.Category;
   import org.springframework.ai.tool.annotation.ToolParam;

   public record PersistTransactionInput(
           @ToolParam(description = "Descrição do gasto") String description,
           @ToolParam(description = "Valor do gasto (em centavos)") long amount,
           @ToolParam(description = "Categoria de uma transação") Category category) {
   }
   ```

2. **[`TransactionOutput.java`](../../src/main/java/dio/budgeting/application/output/TransactionOutput.java)**:
   ```java
   package dio.budgeting.application.output;

   import dio.budgeting.domain.Transaction;
   import java.math.BigDecimal;
   import java.math.RoundingMode;

   public record TransactionOutput(String id, String description, String category, double value) {
       public static TransactionOutput from(Transaction transaction) {
           return new TransactionOutput(
                   transaction.getId().uuid().toString(),
                   transaction.getDescription(),
                   transaction.getCategory().name(),
                   BigDecimal.valueOf(transaction.getAmount()).setScale(2, RoundingMode.HALF_UP).doubleValue());
       }
   }
   ```

3. **[`PersistTransactionUseCase.java`](../../src/main/java/dio/budgeting/application/PersistTransactionUseCase.java)**:
   ```java
   package dio.budgeting.application;

   import dio.budgeting.application.input.PersistTransactionInput;
   import dio.budgeting.application.output.TransactionOutput;
   import dio.budgeting.domain.Transaction;
   import dio.budgeting.domain.TransactionRepository;
   import org.springframework.ai.tool.annotation.Tool;
   import org.springframework.stereotype.Service;

   @Service
   public class PersistTransactionUseCase {
       private final TransactionRepository transactionRepository;

       public PersistTransactionUseCase(TransactionRepository transactionRepository) {
           this.transactionRepository = transactionRepository;
       }

       @Tool(name = "persist-transaction", description = "Persiste uma nova transação financeira")
       public TransactionOutput execute(PersistTransactionInput input) {
           var transaction = transactionRepository.save(
                   new Transaction(input.description(), input.amount(), input.category()));
           return TransactionOutput.from(transaction);
       }
   }
   ```

---

## 3. Validação

```bash
./gradlew test --tests "dio.budgeting.domain.TransactionTest" --tests "dio.budgeting.application.FinancialUseCasesTest"
```
