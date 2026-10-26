package be.ucll.be2.transactions.dto;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferFundsRequest(
        long fromAccountId,
        long toAccountId,
        @Positive BigDecimal amount,
        TransferSpeed speed
) {
    public enum TransferSpeed {
        SLOW,
        FAST
    }

    public TransferFundsRequest {
        if (speed == null) {
            speed = TransferSpeed.FAST;
        }
    }
}
