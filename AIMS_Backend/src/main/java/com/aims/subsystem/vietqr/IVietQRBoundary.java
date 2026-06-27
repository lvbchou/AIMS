package com.aims.subsystem.vietqr;

import com.aims.subsystem.vietqr.dto.QRAccessToken;

import java.io.IOException;

/**
 * Boundary interface for communicating with VietQR services.
 */
public interface IVietQRBoundary {

    /**
     * Requests an access token from VietQR API.
     *
     * @return QRAccessToken containing token information.
     */
    QRAccessToken requestAccessToken();

    /**
     * Generates a payment QR code.
     *
     * @param content transfer content description.
     * @param amount transfer amount.
     * @param orderId order identifier.
     * @param userBankName client bank name.
     * @param accessToken authentication token.
     * @return the QR response payload.
     */
    String getQRCode(String content, long amount, String orderId, String userBankName, String accessToken);

    /**
     * Sends a test callback request for simulation purposes.
     *
     * @param content transfer content description.
     * @param amount transfer amount.
     * @param transType transaction type flag.
     * @param accessToken authentication token.
     * @return response string.
     * @throws IOException network communication error.
     * @throws InterruptedException thread execution interrupted.
     */
    String callTestCallbackApi(String content, long amount, String transType, String accessToken) throws IOException, InterruptedException;

    /**
     * Returns the configured bank code.
     *
     * @return bank code.
     */
    String getBankCode();

    /**
     * Returns the configured bank account.
     *
     * @return bank account.
     */
    String getBankAccount();
}

