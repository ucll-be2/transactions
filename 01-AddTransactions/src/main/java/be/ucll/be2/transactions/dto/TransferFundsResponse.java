package be.ucll.be2.transactions.dto;

import be.ucll.be2.transactions.model.Account;

public record TransferFundsResponse(boolean success, Account fromAccount, Account toAccount) {
}
