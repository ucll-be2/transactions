package be.ucll.be2.transactions.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends Exception {

    private final long accountId;
    private final BigDecimal balance;
    private final BigDecimal requiredBalance;

    public InsufficientFundsException(long accountId, BigDecimal balance, BigDecimal requiredBalance) {
        this.accountId = accountId;
        this.balance = balance;
        this.requiredBalance = requiredBalance;
        super("Account with id " + accountId + " has insufficient funds. Balance: " + balance + ", required: " + requiredBalance);
    }

    public long getAccountId() {
        return accountId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getRequiredBalance() {
        return requiredBalance;
    }

}
