package com.digitalmoneyhouse.account.service;

import com.digitalmoneyhouse.account.dto.AccountDetailsResponse;
import com.digitalmoneyhouse.account.dto.TransactionResponse;
import com.digitalmoneyhouse.account.entity.Account;
import com.digitalmoneyhouse.account.exception.ConflictException;
import com.digitalmoneyhouse.account.exception.ForbiddenException;
import com.digitalmoneyhouse.account.exception.ResourceNotFoundException;
import com.digitalmoneyhouse.account.exception.ValidationException;
import com.digitalmoneyhouse.account.repository.AccountRepository;
import com.digitalmoneyhouse.account.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private static final int MAX_GENERATION_ATTEMPTS = 10;

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountIdentifierGenerator identifierGenerator;

    public AccountService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            AccountIdentifierGenerator identifierGenerator) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.identifierGenerator = identifierGenerator;
    }

    @Transactional
    public AccountDetailsResponse openAccount(Long userId, String email) {
        if (userId == null || email == null || email.isBlank()) {
            throw new ValidationException("El usuario y el email son obligatorios");
        }
        if (accountRepository.existsByUserId(userId)) {
            throw new ValidationException("El usuario ya tiene una cuenta");
        }
        Account account = new Account();
        account.setUserId(userId);
        account.setOwnerEmail(email.trim().toLowerCase());
        account.setCvu(uniqueCvu());
        account.setAlias(uniqueAlias());
        account.setBalance(new BigDecimal("0.00"));
        return AccountDetailsResponse.from(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public AccountDetailsResponse getById(Long id, String email) {
        return AccountDetailsResponse.from(ownedAccount(id, email));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> listTransactions(Long id, String email, Integer limit) {
        if (limit != null && limit < 1) {
            throw new ValidationException("El límite debe ser mayor a cero");
        }
        Account account = ownedAccount(id, email);
        List<TransactionResponse> transactions = transactionRepository.findByAccountIdOrderByCreatedAtDesc(account.getId())
                .stream()
                .map(TransactionResponse::from)
                .toList();
        if (limit == null || limit >= transactions.size()) {
            return transactions;
        }
        return transactions.subList(0, limit);
    }

    @Transactional
    public AccountDetailsResponse updateAlias(Long id, String email, String alias) {
        Account account = ownedAccount(id, email);
        String normalized = alias.trim().toLowerCase();
        if (accountRepository.existsByAliasAndIdNot(normalized, account.getId())) {
            throw new ConflictException("El alias ya está en uso");
        }
        account.setAlias(normalized);
        return AccountDetailsResponse.from(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public AccountDetailsResponse findByUserId(Long userId) {
        return accountRepository.findByUserId(userId)
                .map(AccountDetailsResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("La cuenta no existe"));
    }

    private Account ownedAccount(Long id, String email) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La cuenta no existe"));
        if (!account.getOwnerEmail().equalsIgnoreCase(email)) {
            throw new ForbiddenException("No tenés permiso para ver esta cuenta");
        }
        return account;
    }

    private String uniqueCvu() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String cvu = identifierGenerator.generateCvu();
            if (!accountRepository.existsByCvu(cvu)) {
                return cvu;
            }
        }
        throw new IllegalStateException("No se pudo generar un CVU único");
    }

    private String uniqueAlias() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String alias = identifierGenerator.generateAlias();
            if (!accountRepository.existsByAlias(alias)) {
                return alias;
            }
        }
        throw new IllegalStateException("No se pudo generar un alias único");
    }
}
