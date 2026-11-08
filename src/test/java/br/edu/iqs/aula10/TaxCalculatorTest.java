package br.edu.iqs.aula10;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class TaxCalculatorTest {

    private final TaxCalculator calculator = new TaxCalculator();

    @Test
    void shouldCalculateRegionalTaxesCorrectly() {
        BigDecimal base = new BigDecimal("100.00");

        assertEquals(new BigDecimal("12.00"), calculator.calculateTax(base, "sp"));
        assertEquals(new BigDecimal("14.00"), calculator.calculateTax(base, "RJ"));
        assertEquals(new BigDecimal("10.00"), calculator.calculateTax(base, "MG"));
        assertEquals(new BigDecimal("9.00"), calculator.calculateTax(base, "PR"));
        assertEquals(new BigDecimal("11.00"), calculator.calculateTax(base, "BA"));
        assertEquals(new BigDecimal("0.00"), calculator.calculateTax(BigDecimal.ZERO, "SP"));
    }

    @Test
    void shouldValidateTaxArguments() {
        assertThrows(
                NullPointerException.class,
                () -> calculator.calculateTax(null, "SP"));
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateTax(new BigDecimal("-0.01"), "SP"));
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateTax(BigDecimal.TEN, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateTax(BigDecimal.TEN, "   "));
    }
}
