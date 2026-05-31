// Coupling Level: Data Coupling
// Cohesion Level: Functional Cohesion
// Reason for Coupling: The convert method communicates with the caller using simple arguments (amount, fromCurrency, toCurrency) and is loosely coupled to ExchangeRateProvider using interface-based dependency injection.
// Reason for Cohesion: The class performs exactly one unified responsibility: converting money values from one currency to another using exchange rates.
package com.aims.subsystem.paypal;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CurrencyConverter {

    private final ExchangeRateProvider rateProvider;

    public CurrencyConverter(ExchangeRateProvider rateProvider) {
        this.rateProvider = rateProvider;
    }

    /**
     * Converts an amount from one currency to another.
     *
     * @param amount       The amount to convert
     * @param fromCurrency The original currency (e.g., "VND")
     * @param toCurrency   The target currency (e.g., "USD")
     * @return The converted amount, scaled to 2 decimal places.
     */
    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return amount;
        }

        BigDecimal rate = rateProvider.getExchangeRate(fromCurrency, toCurrency);
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
