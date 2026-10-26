package be.ucll.be2.transactions.controller;

import be.ucll.be2.transactions.dto.TransferFundsRequest;
import be.ucll.be2.transactions.dto.TransferFundsResponse;
import be.ucll.be2.transactions.exception.AccountNotFoundException;
import be.ucll.be2.transactions.exception.InsufficientFundsException;
import be.ucll.be2.transactions.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final AccountService accountService;

    public TransferController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public TransferFundsResponse transferFunds(
            @Valid @RequestBody TransferFundsRequest request
    ) throws AccountNotFoundException, InsufficientFundsException {
        return accountService.transferFunds(
                request.fromAccountId(),
                request.toAccountId(),
                request.amount(),
                request.speed() == TransferFundsRequest.TransferSpeed.SLOW
        );
    }

}
