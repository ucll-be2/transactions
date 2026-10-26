package be.ucll.be2.transactions.controller;

import be.ucll.be2.transactions.dto.CreateAccountRequest;
import be.ucll.be2.transactions.dto.DepositRequest;
import be.ucll.be2.transactions.dto.WithdrawRequest;
import be.ucll.be2.transactions.exception.AccountNotFoundException;
import be.ucll.be2.transactions.exception.InsufficientFundsException;
import be.ucll.be2.transactions.model.Account;
import be.ucll.be2.transactions.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/{accountId}")
    public Account getAccount(@PathVariable long accountId) throws AccountNotFoundException {
        return accountService.getAccount(accountId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Account createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return accountService.createAccount(request.owner());
    }

    @PostMapping("/{accountId}/deposits")
    public Account deposit(
            @PathVariable long accountId,
            @Valid @RequestBody DepositRequest request
    ) throws AccountNotFoundException {
        return accountService.deposit(accountId, request.amount());
    }

    @PostMapping("/{accountId}/withdrawals")
    public Account withdraw(
            @PathVariable long accountId,
            @Valid @RequestBody WithdrawRequest request
    ) throws AccountNotFoundException, InsufficientFundsException {
        return accountService.withdraw(accountId, request.amount());
    }

}
