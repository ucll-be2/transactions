package be.ucll.be2.transactions.exception;

public class AccountNotFoundException extends RuntimeException {

    private final long accountId;

    public AccountNotFoundException(long accountId) {
        this.accountId = accountId;
        super("Account with id " + accountId + " not found");
    }

    public long getAccountId() {
        return accountId;
    }

}
