# Aula 10 — Exposição REST: Implementando o TransactionController

- **Vídeo correspondente**: `Exposição REST Implementando o TransactionController.mp4` (~14m06s)
- **Etapa do Projeto**: Criação do `ListTransactionsByCategoryUseCase`, DTOs HTTP (`TransactionRequest`, `TransactionResponse`) e endpoints REST em `/transactions`.

---

## 🎯 1. Objetivo da Aula

Expor os casos de uso de negócio através de endpoints REST padronizados (`POST /transactions` e `GET /transactions/{category}`), comprovando que a camada `application` funciona independentemente da IA e pode ser consumida tanto por clientes HTTP tradicionais quanto pelo `ChatClient`.

---

## 🛠️ 2. Passo a Passo de Implementação

### Passo 2.1 — Criar o Caso de Uso `ListTransactionsByCategoryUseCase.java`
Crie [`src/main/java/dio/budgeting/application/ListTransactionsByCategoryUseCase.java`](../../src/main/java/dio/budgeting/application/ListTransactionsByCategoryUseCase.java):

```java
package dio.budgeting.application;

import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

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

### Passo 2.2 — Criar os Records de Request e Response HTTP

1. **[`TransactionRequest.java`](../../src/main/java/dio/budgeting/infrastructure/http/request/TransactionRequest.java)**:
   ```java
   package dio.budgeting.infrastructure.http.request;

   import dio.budgeting.application.input.PersistTransactionInput;
   import dio.budgeting.domain.Category;

   public record TransactionRequest(String description, Category category, long amount) {
       public PersistTransactionInput toInput() {
           return new PersistTransactionInput(description, amount, category);
       }
   }
   ```

2. **[`TransactionResponse.java`](../../src/main/java/dio/budgeting/infrastructure/http/response/TransactionResponse.java)**:
   ```java
   package dio.budgeting.infrastructure.http.response;

   import dio.budgeting.application.output.TransactionOutput;

   public record TransactionResponse(String id, String category, String description, double amount) {
       public static TransactionResponse from(TransactionOutput output) {
           return new TransactionResponse(output.id(), output.category(), output.description(), output.value());
       }
   }
   ```

### Passo 2.3 — Implementar os Endpoints REST no `TransactionController`
No [`TransactionController.java`](../../src/main/java/dio/budgeting/infrastructure/http/TransactionController.java), exponha os endpoints REST (incluindo o endpoint de resumo financeiro `GET /transactions/summary` que adicionamos como evolução do desafio):

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

## ▶️ 3. Como Executar e Testar

Com a aplicação rodando (`./gradlew bootRun`), teste os endpoints REST:

```bash
# 1. Criar uma transação
curl -i -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"description": "Supermercado", "category": "GROCERIES", "amount": 12000}'

# 2. Listar por categoria
curl -s http://localhost:8080/transactions/GROCERIES

# 3. Consultar resumo financeiro consolidado (Melhoria implementada)
curl -s http://localhost:8080/transactions/summary
```
