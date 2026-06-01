package com.aims.entity;

/**
 * TransactionStatus - represents the lifecycle status of a payment transaction.
 */
public enum TransactionStatus {
    pending,
    success,
    failed,
    refunded
}
