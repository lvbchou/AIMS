// Coupling Level: Data Coupling
// Cohesion Level: Functional Cohesion
// Reason for Coupling: The interface's single method getExchangeRate only relies on simple string arguments to return a BigDecimal rate.
// Reason for Cohesion: The interface defines a single cohesive abstraction: providing currency exchange rates.
package com.aims.subsystem.paypal;

import java.math.BigDecimal;

/**
 * Interface for providing exchange rates between currencies.
 * This allows for extensibility, e.g., fetching from a database or an external
 * API.
 */
public interface ExchangeRateProvider {
    /**
     * Gets the exchange rate from one currency to another.
     *
     * @param fromCurrency The currency code to convert from (e.g., "VND")
     * @param toCurrency   The currency code to convert to (e.g., "USD")
     * @return The exchange rate
     */
    BigDecimal getExchangeRate(String fromCurrency, String toCurrency);
}
