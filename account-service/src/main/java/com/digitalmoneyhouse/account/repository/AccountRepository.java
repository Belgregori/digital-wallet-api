package com.digitalmoneyhouse.account.repository;

import com.digitalmoneyhouse.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByCvu(String cvu);

    boolean existsByAlias(String alias);

    boolean existsByAliasAndIdNot(String alias, Long id);

    boolean existsByUserId(Long userId);

    Optional<Account> findByUserId(Long userId);
}
