package dio.budgeting.application.output;

import dio.budgeting.domain.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record FinancialSummaryOutput(
        long totalTransactions,
        double totalAmount,
        Map<String, Double> totalByCategory
) {
    public static FinancialSummaryOutput from(List<Transaction> transactions) {
        long count = transactions.size();
        long totalSum = transactions.stream().mapToLong(Transaction::getAmount).sum();

        Map<String, Double> byCategory = transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().name(),
                        Collectors.collectingAndThen(
                                Collectors.summingLong(Transaction::getAmount),
                                sum -> BigDecimal.valueOf(sum).setScale(2, RoundingMode.HALF_UP).doubleValue()
                        )
                ));

        return new FinancialSummaryOutput(
                count,
                BigDecimal.valueOf(totalSum).setScale(2, RoundingMode.HALF_UP).doubleValue(),
                byCategory
        );
    }
}
