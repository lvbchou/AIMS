package com.aims.subsystem.paypal.config;

import com.aims.subsystem.paypal.CurrencyConverter;
import com.aims.subsystem.paypal.ExchangeRateProvider;
import com.aims.subsystem.paypal.FixedExchangeRateProvider;
import com.aims.subsystem.paypal.PayPalBoundary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.net.http.HttpClient;

/**
 * PayPalConfig — Spring {@code @Configuration} class that declares all PayPal subsystem
 * collaborators as managed beans.
 *
 * <p><strong>DIP (Dependency Inversion Principle):</strong> High-level classes
 * ({@link com.aims.subsystem.paypal.PayPalController}) receive their dependencies
 * through constructor injection rather than instantiating them with {@code new}.
 * This file is the single place where concrete implementations are wired together.</p>
 *
 * <p>Pattern: <em>Dependency Injection / IoC Container</em> — aligns with the
 * Hollywood Principle: "Don't call us, we'll call you."</p>
 *
 * <h3>Changes in this revision</h3>
 * <ul>
 *   <li><strong>R3:</strong> Declares a shared {@link HttpClient} bean so that
 *       {@link com.aims.subsystem.paypal.PayPalBoundary} receives it via injection
 *       rather than self-instantiating it.</li>
 *   <li><strong>R6:</strong> Reads exchange rates from {@code application.properties}
 *       ({@code exchange.rate.VND.USD} / {@code exchange.rate.USD.VND}) and passes them
 *       to {@link FixedExchangeRateProvider}, eliminating the hardcoded constructor values.</li>
 * </ul>
 */
@Configuration
public class PayPalConfig {

    /**
     * Declares a shared, reusable {@link HttpClient} bean (R3).
     *
     * <p>A single instance is shared across all PayPal boundary calls. {@link HttpClient}
     * is designed to be created once and reused — it manages its own connection pool
     * internally.</p>
     *
     * @return a new default {@link HttpClient}.
     */
    @Bean
    public HttpClient paypalHttpClient() {
        return HttpClient.newHttpClient();
    }

    /**
     * Declares the raw HTTP boundary as a Spring bean so it can be injected
     * into {@link com.aims.subsystem.paypal.PayPalAuthManager} and
     * {@link com.aims.subsystem.paypal.PayPalController}.
     *
     * <p><strong>R3:</strong> The {@link HttpClient} is now injected rather than
     * self-instantiated, making {@link PayPalBoundary} unit-testable.</p>
     *
     * @param baseUrl    the PayPal REST API base URL read from {@code application.properties}.
     * @param httpClient the shared HTTP client declared by {@link #paypalHttpClient()}.
     * @return a configured {@link PayPalBoundary} instance.
     */
    @Bean
    public PayPalBoundary payPalBoundary(
            @Value("${paypal.base.url}") String baseUrl,
            HttpClient httpClient) {
        return new PayPalBoundary(baseUrl, httpClient);
    }

    /**
     * Declares the fixed exchange-rate provider as a Spring bean (R6).
     *
     * <p><strong>Strategy Pattern (HFDP Ch. 1):</strong> {@link ExchangeRateProvider} is the
     * strategy interface; this bean selects {@link FixedExchangeRateProvider} as the concrete
     * strategy. Swapping to a live-rate provider requires only changing this method — zero
     * changes to the converter or controller.</p>
     *
     * <p><strong>R6:</strong> Exchange rates are now read from {@code application.properties}
     * ({@code exchange.rate.VND.USD} and {@code exchange.rate.USD.VND}) instead of being
     * hardcoded in the constructor. Rate updates no longer require a recompile.</p>
     *
     * @param vndToUsd VND → USD rate (e.g. {@code 0.00004}).
     * @param usdToVnd USD → VND rate (e.g. {@code 25000}).
     * @return a {@link FixedExchangeRateProvider} initialised with the configured rates.
     */
    @Bean
    public ExchangeRateProvider exchangeRateProvider(
            @Value("${exchange.rate.VND.USD}") BigDecimal vndToUsd,
            @Value("${exchange.rate.USD.VND}") BigDecimal usdToVnd) {
        return new FixedExchangeRateProvider(vndToUsd, usdToVnd);
    }

    /**
     * Declares the currency converter as a Spring bean.
     *
     * <p><strong>DIP adherence:</strong> the converter depends on the
     * {@link ExchangeRateProvider} abstraction, not on any concrete implementation.</p>
     *
     * @param rateProvider the rate source strategy injected by Spring.
     * @return a {@link CurrencyConverter} backed by the given rate provider.
     */
    @Bean
    public CurrencyConverter currencyConverter(ExchangeRateProvider rateProvider) {
        return new CurrencyConverter(rateProvider);
    }
}
