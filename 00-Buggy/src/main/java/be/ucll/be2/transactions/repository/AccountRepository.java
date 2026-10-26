package be.ucll.be2.transactions.repository;

import be.ucll.be2.transactions.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
