package be.ucll.be2.transactions.service;

import be.ucll.be2.transactions.dto.TransferFundsResponse;
import be.ucll.be2.transactions.exception.AccountNotFoundException;
import be.ucll.be2.transactions.exception.InsufficientFundsException;
import be.ucll.be2.transactions.model.Account;
import be.ucll.be2.transactions.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Account getAccount(long accountId) throws AccountNotFoundException {
        return accountRepository.findById(accountId).orElseThrow(
                () -> new AccountNotFoundException(accountId)
        );
    }

    @Transactional
    public Account createAccount(String owner) {
        return accountRepository.save(new Account(owner));
    }

    private void sleep() {
        try {
            Thread.sleep(Duration.ofSeconds(10));
        } catch (InterruptedException ignored) {
        }
    }

    @Transactional(rollbackFor = {AccountNotFoundException.class, InsufficientFundsException.class})
    public TransferFundsResponse transferFunds(
            long fromAccountId,
            long toAccountId,
            BigDecimal amount,
            boolean slow
    ) throws AccountNotFoundException, InsufficientFundsException {
        // Note: getAccount can throw AccountNotFoundException
        final var fromAccount = getAccount(fromAccountId);
        // Note: withdraw can throw InsufficientFundsException
        fromAccount.withdraw(amount);
        if (slow) {
            sleep();
        }
        accountRepository.save(fromAccount);

        // Note: getAccount can throw AccountNotFoundException
        final var toAccount = getAccount(toAccountId);
        toAccount.deposit(amount);
        accountRepository.save(toAccount);

        return new TransferFundsResponse(true, fromAccount, toAccount);
    }

    @Transactional(rollbackFor = {AccountNotFoundException.class})
    public Account deposit(long accountId, BigDecimal amount) throws AccountNotFoundException {
        // Note: getAccount can throw AccountNotFoundException
        final var account = getAccount(accountId);
        account.deposit(amount);
        accountRepository.save(account);
        return account;
    }

    @Transactional(rollbackFor = {AccountNotFoundException.class, InsufficientFundsException.class})
    public Account withdraw(long accountId, BigDecimal amount) throws AccountNotFoundException, InsufficientFundsException {
        // Note: getAccount can throw AccountNotFoundException
        final var account = getAccount(accountId);
        // Note: withdraw can throw InsufficientFundsException
        account.withdraw(amount);
        accountRepository.save(account);
        return account;
    }
}
