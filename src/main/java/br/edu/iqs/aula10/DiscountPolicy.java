package br.edu.iqs.aula10;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Politica de calculo de descontos cumulativos (fidelidade, volume e cupom promocional)
 * com teto maximo de 30% sobre o subtotal.
 */
public final class DiscountPolicy {

    private static final BigDecimal VOLUME_BONUS_RATE = new BigDecimal("0.05");
    private static final BigDecimal COUPON_IQS10_RATE = new BigDecimal("0.10");
    private static final BigDecimal COUPON_CI5_RATE = new BigDecimal("0.05");
    private static final BigDecimal MAX_DISCOUNT_RATE = new BigDecimal("0.30");
    private static final int VOLUME_BONUS_THRESHOLD = 10;

    public BigDecimal calculateDiscount(
            BigDecimal subtotal,
            CustomerTier tier,
            int totalUnits,
            String couponCode) {
        Objects.requireNonNull(subtotal, "subtotal nao pode ser nulo");
        Objects.requireNonNull(tier, "tier nao pode ser nulo");
        if (subtotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("subtotal nao pode ser negativo");
        }
        if (totalUnits < 0) {
            throw new IllegalArgumentException("totalUnits nao pode ser negativo");
        }

        BigDecimal rate = tier.getDiscountRate();
        if (totalUnits >= VOLUME_BONUS_THRESHOLD) {
            rate = rate.add(VOLUME_BONUS_RATE);
        }
        rate = rate.add(resolveCouponRate(couponCode));

        if (rate.compareTo(MAX_DISCOUNT_RATE) > 0) {
            rate = MAX_DISCOUNT_RATE;
        }

        return subtotal.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal resolveCouponRate(String couponCode) {
        if (couponCode == null || couponCode.isBlank()) {
            return BigDecimal.ZERO;
        }
        String normalized = couponCode.trim().toUpperCase();
        switch (normalized) {
            case "IQS10":
                return COUPON_IQS10_RATE;
            case "CI5":
                return COUPON_CI5_RATE;
            default:
                throw new IllegalArgumentException("Cupom promocional invalido: " + couponCode);
        }
    }
}
