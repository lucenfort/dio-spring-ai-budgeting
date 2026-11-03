# 10 — Exposição REST: TransactionController e Casos de Uso de Consulta

- **Escopo**: Implementação de `ListTransactionsByCategoryUseCase`, `GetFinancialSummaryUseCase`, DTOs HTTP (`TransactionRequest`, `TransactionResponse`) e endpoints REST em `/transactions`.

---

## 1. Objetivo

Expor os casos de uso da aplicação via endpoints HTTP REST (`POST /transactions`, `GET /transactions/{category}` e `GET /transactions/summary`).

---

## 2. Implementação

### 2.1. Casos de Uso de Consulta
- **[`ListTransactionsByCategoryUseCase.java`](../../src/main/java/dio/budgeting/application/ListTransactionsByCategoryUseCase.java)**:
  ```java
  @Service
  public class ListTransactionsByCategoryUseCase {
      private final TransactionRepository transactionRepository;

      public ListTransactionsByCategoryUseCase(TransactionRepository transactionRepository) {
          this.transactionRepository = transactionRepository;
      }

      @Tool(name = "list-transactions-by-category", description = "Lista transações financeiras por categoria")
      public List<TransactionOutput> execute(@ToolParam(description = "Categoria de uma transação") Category category) {
          return transactionRepository.findAllByCategory(category).stream().map(TransactionOutput::from).toList();
      }
  }
  ```

- **[`GetFinancialSummaryUseCase.java`](../../src/main/java/dio/budgeting/application/GetFinancialSummaryUseCase.java)**:
  ```java
  @Service
  public class GetFinancialSummaryUseCase {
      private final TransactionRepository transactionRepository;

      public GetFinancialSummaryUseCase(TransactionRepository transactionRepository) {
          this.transactionRepository = transactionRepository;
      }

      @Tool(name = "get-financial-summary", description = "Retorna o resumo financeiro consolidado com o total geral gasto, quantidade de transações e soma por categoria")
      public FinancialSummaryOutput execute() {
          var transactions = transactionRepository.findAll();
          double totalAmount = transactions.stream()
                  .mapToLong(Transaction::getAmount)
                  .mapToDouble(amount -> BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP).doubleValue())
                  .sum();

          Map<String, Double> totalByCategory = transactions.stream()
                  .collect(Collectors.groupingBy(
                          t -> t.getCategory().name(),
                          Collectors.summingDouble(t -> BigDecimal.valueOf(t.getAmount()).setScale(2, RoundingMode.HALF_UP).doubleValue())
                  ));

          return new FinancialSummaryOutput(transactions.size(), totalAmount, totalByCategory);
      }
  }
  ```

### 2.2. DTOs HTTP e Endpoints no `TransactionController`
Em [`TransactionController.java`](../../src/main/java/dio/budgeting/infrastructure/http/TransactionController.java):

```java
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
```

---

## 3. Validação

```bash
curl -i -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"description": "Supermercado", "category": "GROCERIES", "amount": 12000}'

curl -s http://localhost:8080/transactions/GROCERIES
curl -s http://localhost:8080/transactions/summary
```
