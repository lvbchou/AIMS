// Coupling Level: Data Coupling
// Cohesion Level: Functional Cohesion
// Reason for Coupling: Its operations depend only on standard types like String and BigDecimal, with no references to other subsystem classes.
// Reason for Cohesion: All attributes (in-memory rates map) and methods (setRate, getExchangeRate) are entirely aligned around managing local currency exchange rates.
/**
 * SOLID Principles Analysis:
 * - **OCP (Open/Closed Principle) Adherence & Limitation**: Implements the abstraction correctly, but hardcodes rate conversions in the constructor.
 * 
 * **Improvement Direction**: Load default rate values from configuration properties or database settings to make it fully open for extension without recompiling.
 */
package com.aims.subsystem.paypal;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * A basic implementation of ExchangeRateProvider that uses fixed, in-memory
 * rates.
 */
public class FixedExchangeRateProvider implements ExchangeRateProvider {

    private final Map<String, BigDecimal> rates = new HashMap<>();

    public FixedExchangeRateProvider() {
        // Default required rate for the project: VND to USD
        // Assuming an example rate of 1 USD = 25000 VND => 1 VND = 0.00004 USD
        setRate("VND", "USD", new BigDecimal("0.00004"));
        setRate("USD", "VND", new BigDecimal("25000"));
    }

    /**
     * Sets a fixed exchange rate between two currencies.
     */
    public void setRate(String fromCurrency, String toCurrency, BigDecimal rate) {
        rates.put(fromCurrency.toUpperCase() + "_" + toCurrency.toUpperCase(), rate);
    }

    @Override
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        String key = fromCurrency.toUpperCase() + "_" + toCurrency.toUpperCase();
        if (rates.containsKey(key)) {
            return rates.get(key);
        }
        throw new IllegalArgumentException("Exchange rate not found for " + fromCurrency + " to " + toCurrency);
    }
}
