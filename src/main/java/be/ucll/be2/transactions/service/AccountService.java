package be.ucll.be2.transactions.service;

import be.ucll.be2.transactions.dto.TransferFundsRequest;
import be.ucll.be2.transactions.dto.TransferFundsResponse;
import be.ucll.be2.transactions.exception.AccountNotFoundException;
import be.ucll.be2.transactions.model.Account;
import be.ucll.be2.transactions.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account getAccount(long accountId) {
        return accountRepository.findById(accountId).orElseThrow(
                () -> new AccountNotFoundException(accountId)
        );
    }

    public Account createAccount(String owner) {
        return accountRepository.save(new Account(owner));
    }

    private void sleep() {
        try {
            Thread.sleep(Duration.ofSeconds(10));
        } catch (InterruptedException ignored) {
        }
    }

    public TransferFundsResponse transferFunds(long fromAccountId,
                                               long toAccountId,
                                               BigDecimal amount,
                                               boolean slow) {
        final var fromAccount = getAccount(fromAccountId);
        fromAccount.withdraw(amount);
        if (slow) {
            sleep();
        }
        accountRepository.save(fromAccount);

        final var toAccount = getAccount(toAccountId);
        toAccount.deposit(amount);
        accountRepository.save(toAccount);

        return new TransferFundsResponse(true, fromAccount, toAccount);
    }

    public Account deposit(long accountId, BigDecimal amount) {
        final var account = getAccount(accountId);
        account.deposit(amount);
        accountRepository.save(account);
        return account;
    }

    public Account withdraw(long accountId, BigDecimal amount) {
        final var account = getAccount(accountId);
        account.withdraw(amount);
        accountRepository.save(account);
        return account;
    }
}
