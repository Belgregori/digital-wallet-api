package com.digitalmoneyhouse.account.service;

import com.digitalmoneyhouse.account.entity.Account;
import com.digitalmoneyhouse.account.entity.Transaction;
import com.digitalmoneyhouse.account.exception.ForbiddenException;
import com.digitalmoneyhouse.account.exception.ResourceNotFoundException;
import com.digitalmoneyhouse.account.repository.AccountRepository;
import com.digitalmoneyhouse.account.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountIdentifierGenerator identifierGenerator;

    @InjectMocks
    private AccountService accountService;

    @Test
    void openAccountStoresZeroBalanceAndGeneratedIdentifiers() {
        when(identifierGenerator.generateCvu()).thenReturn("1234567890123456789012");
        when(identifierGenerator.generateAlias()).thenReturn("sol.luna.rio");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setUserId(account.getUserId());
            return withId(account, 8L);
        });

        var response = accountService.openAccount(4L, " Ana@Example.com ");

        assertEquals(8L, response.id());
        assertEquals(4L, response.userId());
        assertEquals("1234567890123456789012", response.cvu());
        assertEquals("sol.luna.rio", response.alias());
        assertEquals(new BigDecimal("0.00"), response.balance());
    }

    @Test
    void getByIdReturnsBalanceForTheOwner() {
        when(accountRepository.findById(8L)).thenReturn(Optional.of(account()));

        var response = accountService.getById(8L, "ana@example.com");

        assertEquals(new BigDecimal("150.50"), response.balance());
        assertEquals("1234567890123456789012", response.cvu());
    }

    @Test
    void getByIdRejectsAnotherUser() {
        when(accountRepository.findById(8L)).thenReturn(Optional.of(account()));

        assertThrows(ForbiddenException.class, () -> accountService.getById(8L, "otro@example.com"));
    }

    @Test
    void listTransactionsKeepsNewestFirstAndAppliesLimit() {
        when(accountRepository.findById(8L)).thenReturn(Optional.of(account()));
        when(transactionRepository.findByAccountIdOrderByCreatedAtDesc(8L)).thenReturn(List.of(
                transaction(3L, "mas nueva"),
                transaction(2L, "media"),
                transaction(1L, "mas vieja")));

        var all = accountService.listTransactions(8L, "ana@example.com", null);
        var latest = accountService.listTransactions(8L, "ana@example.com", 1);

        assertEquals(List.of(3L, 2L, 1L), all.stream().map(item -> item.id()).toList());
        assertEquals("mas nueva", latest.get(0).description());
    }

    @Test
    void getByIdReturnsNotFoundWhenMissing() {
        when(accountRepository.findById(8L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.getById(8L, "ana@example.com"));
    }

    @Test
    void cvuHasTwentyTwoDigitsWhenGenerated() {
        AccountIdentifierGenerator generator = new AccountIdentifierGenerator(java.util.List.of("sol", "luna", "rio"));
        String cvu = generator.generateCvu();
        assertEquals(22, cvu.length());
        assertTrue(cvu.chars().allMatch(Character::isDigit));
    }

    private Account account() {
        Account account = new Account();
        account.setUserId(4L);
        account.setOwnerEmail("ana@example.com");
        account.setCvu("1234567890123456789012");
        account.setAlias("sol.luna.rio");
        account.setBalance(new BigDecimal("150.50"));
        return withId(account, 8L);
    }

    private Transaction transaction(Long id, String description) {
        Transaction transaction = new Transaction();
        transaction.setAccountId(8L);
        transaction.setAmount(new BigDecimal("10.00"));
        transaction.setCreatedAt(Instant.parse("2026-11-19T12:00:00Z"));
        transaction.setDescription(description);
        try {
            var field = Transaction.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(transaction, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
        return transaction;
    }

    private Account withId(Account account, Long id) {
        try {
            var field = Account.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(account, id);
            return account;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
