package com.aims.gateway;

import com.aims.dto.payment.GatewayTransactionContext;
import com.aims.dto.payment.GatewayTransactionResult;
import com.aims.gateway.PaymentCaptureParams;
import com.aims.gateway.PaymentInitiateParams;
import com.aims.gateway.PaymentRefundParams;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * PaymentGatewayLoggingAspect — cross-cutting concern: structured logging and
 * execution timing for every {@link IPaymentGateway} invocation (R7).
 *
 * <h3>Design Rationale</h3>
 * <p><strong>Open-Closed Principle:</strong> Adding logging to all current and
 * future payment gateways requires <em>zero modification</em> to any gateway
 * class. Any new class that implements {@link IPaymentGateway} and is
 * annotated {@code @Component} is automatically covered by this advice the
 * moment it is registered in the Spring context.</p>
 *
 * <p>Without AOP, logging would have to be added to every concrete
 * {@code IPaymentGateway} implementation — a change that violates OCP and
 * creates code duplication.</p>
 *
 * <h3>What is logged</h3>
 * <ul>
 *   <li>The concrete gateway class name (e.g. {@code PayPalController}).</li>
 *   <li>The method being called ({@code createPayment} / {@code completePayment}).</li>
 *   <li>Key parameters (invoice ID / order ID) — never credentials or tokens.</li>
 *   <li>Execution time in milliseconds.</li>
 *   <li>Success flag for {@code completePayment} results.</li>
 *   <li>Exception class and message on failure (at ERROR level).</li>
 * </ul>
 *
 * <h3>Requires</h3>
 * <p>{@code implementation 'org.springframework.boot:spring-boot-starter-aop'}
 * in {@code build.gradle}.</p>
 */
@Aspect
@Component
public class PaymentGatewayLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(PaymentGatewayLoggingAspect.class);

    /**
     * Wraps every method declared on {@link IPaymentGateway} and {@link IRefundableGateway} implementations.
     *
     * <p>Intercept target: any method on any Spring bean that implements
     * {@link IPaymentGateway} or {@link IRefundableGateway}. This covers all current and future gateway
     * implementations without requiring per-class changes.</p>
     *
     * @param pjp the join point representing the intercepted method call.
     * @return the return value of the intercepted method.
     * @throws Throwable re-throws any exception the gateway method throws.
     */
    @Around("execution(* com.aims.gateway.IPaymentGateway.*(..)) || execution(* com.aims.gateway.IRefundableGateway.*(..))")
    public Object logAndTime(ProceedingJoinPoint pjp) throws Throwable {
        String gatewayClass  = pjp.getTarget().getClass().getSimpleName();
        String methodName    = pjp.getSignature().getName();
        Object[] args        = pjp.getArgs();
 
        // Log entry — extract key param without exposing sensitive data
        String paramSummary = summariseParams(methodName, args);
        log.info("[PaymentGateway] {}.{}() called — {}", gatewayClass, methodName, paramSummary);
 
        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            long elapsed = System.currentTimeMillis() - start;
 
            // Log exit — include a safe summary of the result
            String resultSummary = summariseResult(methodName, result);
            log.info("[PaymentGateway] {}.{}() completed in {} ms — {}",
                    gatewayClass, methodName, elapsed, resultSummary);
 
            return result;
 
        } catch (Throwable t) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[PaymentGateway] {}.{}() FAILED after {} ms — {}: {}",
                    gatewayClass, methodName, elapsed,
                    t.getClass().getSimpleName(), t.getMessage());
            throw t;
        }
    }
 
    // -------------------------------------------------------------------------
    // Private helpers — produce log-safe summaries
    // -------------------------------------------------------------------------
 
    private static String summariseParams(String methodName, Object[] args) {
        if (args == null || args.length == 0) return "(no args)";
        return switch (methodName) {
            case "createPayment" -> {
                if (args[0] instanceof PaymentInitiateParams p)
                    yield "invoiceId=" + p.invoiceId() + ", currency=" + p.currency()
                            + ", amount=" + p.amount();
                yield args[0].toString();
            }
            case "completePayment" -> {
                if (args[0] instanceof PaymentCaptureParams p)
                    yield "orderId=" + p.orderId();   // token omitted intentionally
                yield args[0].toString();
            }
            case "refundPayment" -> {
                if (args[0] instanceof PaymentRefundParams p)
                    yield "transactionId=" + p.transactionId() + ", currency=" + p.currency()
                            + ", amount=" + p.amount();
                yield args[0].toString();
            }
            case "getSupportedMethod" -> "(no args)";
            default -> "(args redacted)";
        };
    }
 
    private static String summariseResult(String methodName, Object result) {
        if (result == null) return "null";
        return switch (methodName) {
            case "createPayment" -> {
                if (result instanceof GatewayTransactionContext ctx)
                    yield "gatewayOrderId=" + ctx.getGatewayOrderId()
                            + ", approvalUrl=" + (ctx.getApproveUrl() != null ? "[present]" : "null");
                yield result.toString();
            }
            case "completePayment" -> {
                if (result instanceof GatewayTransactionResult r)
                    yield "success=" + r.checkSuccess()
                            + ", status=" + r.getStatus()
                            + ", transactionId=" + r.getTransactionId();
                yield result.toString();
            }
            case "refundPayment" -> {
                if (result instanceof com.aims.dto.GatewayRefundResult r)
                    yield "success=" + r.isSuccess()
                            + ", status=" + r.getStatus()
                            + ", refundId=" + r.getRefundId();
                yield result.toString();
            }
            default -> result.toString();
        };
    }
}
