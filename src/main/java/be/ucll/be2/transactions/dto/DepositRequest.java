package be.ucll.be2.transactions.dto;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DepositRequest(@Positive BigDecimal amount) {
}
