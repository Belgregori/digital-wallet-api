package com.digitalmoneyhouse.users.repository;

import com.digitalmoneyhouse.users.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByCvu(String cvu);

    boolean existsByAlias(String alias);
}
