package br.edu.iqs.aula10;

import java.math.BigDecimal;
import java.util.List;

/**
 * Classe intencionalmente adicionada SEM testes unitarios na branch simulacao/pr-baixa-cobertura
 * para demonstrar o bloqueio automatico de Pull Request pelo Quality Gate (Atividade 3).
 */
public final class UncoveredLegacyReportGenerator {

    public String generateDetailedAuditReport(List<OrderSummary> summaries, String title) {
        StringBuilder builder = new StringBuilder();
        String safeTitle = title == null || title.isBlank() ? "RELATORIO PADRAO" : title.trim();
        builder.append("=== ").append(safeTitle).append(" ===\n");

        if (summaries == null || summaries.isEmpty()) {
            builder.append("Nenhum pedido registrado no periodo.\n");
            return builder.toString();
        }

        BigDecimal grossRevenue = BigDecimal.ZERO;
        BigDecimal totalDiscounts = BigDecimal.ZERO;
        BigDecimal totalTaxes = BigDecimal.ZERO;
        BigDecimal netRevenue = BigDecimal.ZERO;
        int paidCount = 0;
        int cancelledCount = 0;
        int pendingCount = 0;
        int shippedCount = 0;

        for (OrderSummary item : summaries) {
            grossRevenue = grossRevenue.add(item.getSubtotal());
            totalDiscounts = totalDiscounts.add(item.getDiscount());
            totalTaxes = totalTaxes.add(item.getTax());
            netRevenue = netRevenue.add(item.getFinalTotal());

            switch (item.getStatus()) {
                case PAID:
                    paidCount++;
                    break;
                case CANCELLED:
                    cancelledCount++;
                    break;
                case SHIPPED:
                    shippedCount++;
                    break;
                default:
                    pendingCount++;
                    break;
            }

            builder.append("Pedido: ").append(item.getOrderId())
                    .append(" | Cliente: ").append(item.getCustomerId())
                    .append(" | Subtotal: ").append(item.getSubtotal())
                    .append(" | Desconto: ").append(item.getDiscount())
                    .append(" | Imposto: ").append(item.getTax())
                    .append(" | Final: ").append(item.getFinalTotal())
                    .append(" | Status: ").append(item.getStatus())
                    .append('\n');
        }

        builder.append("--- CONSOLIDADO ---\n");
        builder.append("Bruto: ").append(grossRevenue).append('\n');
        builder.append("Descontos: ").append(totalDiscounts).append('\n');
        builder.append("Impostos: ").append(totalTaxes).append('\n');
        builder.append("Liquido: ").append(netRevenue).append('\n');
        builder.append("Pagos: ").append(paidCount).append('\n');
        builder.append("Enviados: ").append(shippedCount).append('\n');
        builder.append("Pendentes: ").append(pendingCount).append('\n');
        builder.append("Cancelados: ").append(cancelledCount).append('\n');
        builder.append("Classificacao: ").append(classifyPerformance(netRevenue)).append('\n');
        return builder.toString();
    }

    public String classifyPerformance(BigDecimal netRevenue) {
        if (netRevenue == null || netRevenue.compareTo(BigDecimal.ZERO) <= 0) {
            return "SEM_RECEITA";
        }
        if (netRevenue.compareTo(new BigDecimal("1000.00")) < 0) {
            return "BRONZE";
        }
        if (netRevenue.compareTo(new BigDecimal("5000.00")) < 0) {
            return "PRATA";
        }
        if (netRevenue.compareTo(new BigDecimal("20000.00")) < 0) {
            return "OURO";
        }
        return "DIAMANTE";
    }
}
