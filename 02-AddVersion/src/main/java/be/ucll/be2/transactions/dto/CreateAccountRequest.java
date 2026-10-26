package be.ucll.be2.transactions.dto;

import be.ucll.be2.transactions.model.Account;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for {@link Account}
 */
public record CreateAccountRequest(@NotBlank String owner) {
}
