package br.edu.iqs.aula10;

import java.math.BigDecimal;

/**
 * Niveis de fidelidade do cliente com percentual base de desconto.
 */
public enum CustomerTier {

    STANDARD(new BigDecimal("0.00")),
    SILVER(new BigDecimal("0.05")),
    GOLD(new BigDecimal("0.10")),
    PLATINUM(new BigDecimal("0.15"));

    private final BigDecimal discountRate;

    CustomerTier(BigDecimal discountRate) {
        this.discountRate = discountRate;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }
}
