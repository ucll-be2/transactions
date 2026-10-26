package be.ucll.be2.transactions.controller;

import be.ucll.be2.transactions.exception.AccountNotFoundException;
import be.ucll.be2.transactions.exception.InsufficientFundsException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.math.BigDecimal;

@RestControllerAdvice
public class ExceptionHandlers {

    @JsonPropertyOrder({"message", "account"})
    public record AccountNotFoundExceptionResponse(String message, long accountId) {
    }

    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public AccountNotFoundExceptionResponse handleAccountNotFoundException(AccountNotFoundException ex) {
        return new AccountNotFoundExceptionResponse(ex.getMessage(), ex.getAccountId());
    }

    @JsonPropertyOrder({"message", "account", "balance", "required"})
    public record InsufficientFundsExceptionResponse(String message,
                                                     long accountId,
                                                     BigDecimal balance,
                                                     BigDecimal requiredBalance) {
    }

    @ExceptionHandler(InsufficientFundsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public InsufficientFundsExceptionResponse handleAccountNotFoundException(InsufficientFundsException ex) {
        return new InsufficientFundsExceptionResponse(ex.getMessage(), ex.getAccountId(), ex.getBalance(), ex.getRequiredBalance());
    }

}
