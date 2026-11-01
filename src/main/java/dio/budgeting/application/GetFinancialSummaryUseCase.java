package dio.budgeting.application;

import dio.budgeting.application.output.FinancialSummaryOutput;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class GetFinancialSummaryUseCase {
    private final TransactionRepository transactionRepository;

    public GetFinancialSummaryUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool(
            name = "get-financial-summary",
            description = "Obtém o resumo financeiro consolidado com a quantidade total de transações, o valor total gasto e o subtotal agrupado por categoria"
    )
    public FinancialSummaryOutput execute() {
        var transactions = transactionRepository.findAll();
        return FinancialSummaryOutput.from(transactions);
    }
}
