package br.edu.iqs.aula10;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Calculadora de impostos estaduais incidentes sobre a base tributavel do pedido.
 */
public final class TaxCalculator {

    private static final BigDecimal RATE_SP = new BigDecimal("0.12");
    private static final BigDecimal RATE_RJ = new BigDecimal("0.14");
    private static final BigDecimal RATE_MG = new BigDecimal("0.10");
    private static final BigDecimal RATE_PR = new BigDecimal("0.09");
    private static final BigDecimal RATE_DEFAULT = new BigDecimal("0.11");

    public BigDecimal calculateTax(BigDecimal taxableAmount, String stateCode) {
        Objects.requireNonNull(taxableAmount, "taxableAmount nao pode ser nulo");
        if (taxableAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("taxableAmount nao pode ser negativo");
        }
        if (stateCode == null || stateCode.isBlank()) {
            throw new IllegalArgumentException("stateCode nao pode ser vazio");
        }
        if (taxableAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal rate = resolveStateRate(stateCode.trim().toUpperCase());
        return taxableAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal resolveStateRate(String normalizedState) {
        switch (normalizedState) {
            case "SP":
                return RATE_SP;
            case "RJ":
                return RATE_RJ;
            case "MG":
                return RATE_MG;
            case "PR":
                return RATE_PR;
            default:
                return RATE_DEFAULT;
        }
    }
}
