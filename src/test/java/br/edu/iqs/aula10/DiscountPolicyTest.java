package br.edu.iqs.aula10;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class DiscountPolicyTest {

    private final DiscountPolicy policy = new DiscountPolicy();

    @Test
    void shouldApplyTierVolumeAndCouponCappedAtThirtyPercent() {
        assertEquals(
                new BigDecimal("0.00"),
                policy.calculateDiscount(new BigDecimal("200.00"), CustomerTier.STANDARD, 2, null));
        assertEquals(
                new BigDecimal("10.00"),
                policy.calculateDiscount(new BigDecimal("200.00"), CustomerTier.SILVER, 2, "  "));
        assertEquals(
                new BigDecimal("30.00"),
                policy.calculateDiscount(new BigDecimal("200.00"), CustomerTier.GOLD, 10, null));
        assertEquals(
                new BigDecimal("40.00"),
                policy.calculateDiscount(new BigDecimal("200.00"), CustomerTier.GOLD, 5, "iqs10"));
        assertEquals(
                new BigDecimal("30.00"),
                policy.calculateDiscount(new BigDecimal("200.00"), CustomerTier.GOLD, 5, "CI5"));
        // PLATINUM (15%) + volume (5%) + IQS10 (10%) = 30% -> 60.00
        assertEquals(
                new BigDecimal("60.00"),
                policy.calculateDiscount(new BigDecimal("200.00"), CustomerTier.PLATINUM, 15, "IQS10"));
    }

    @Test
    void shouldEnforceMaximumThirtyPercentCapWhenRatesExceedLimit() {
        // Caso o calculo bruto tente ultrapassar 30%, garante respeito ao teto
        BigDecimal discount = policy.calculateDiscount(
                new BigDecimal("1000.00"),
                CustomerTier.PLATINUM,
                25,
                "IQS10");
        assertEquals(new BigDecimal("300.00"), discount);
    }

    @Test
    void shouldRejectInvalidInputsAndUnknownCoupons() {
        assertThrows(
                NullPointerException.class,
                () -> policy.calculateDiscount(null, CustomerTier.STANDARD, 1, null));
        assertThrows(
                NullPointerException.class,
                () -> policy.calculateDiscount(BigDecimal.TEN, null, 1, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> policy.calculateDiscount(new BigDecimal("-1.00"), CustomerTier.STANDARD, 1, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> policy.calculateDiscount(BigDecimal.TEN, CustomerTier.STANDARD, -1, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> policy.calculateDiscount(BigDecimal.TEN, CustomerTier.STANDARD, 1, "INVALIDO"));
    }
}
