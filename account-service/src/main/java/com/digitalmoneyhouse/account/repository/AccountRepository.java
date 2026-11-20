package com.digitalmoneyhouse.account.repository;

import com.digitalmoneyhouse.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByCvu(String cvu);

    boolean existsByAlias(String alias);

    boolean existsByUserId(Long userId);
}
