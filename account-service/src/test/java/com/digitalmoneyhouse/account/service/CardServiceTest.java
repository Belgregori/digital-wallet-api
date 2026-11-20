package com.digitalmoneyhouse.account.service;

import com.digitalmoneyhouse.account.dto.CardRequest;
import com.digitalmoneyhouse.account.entity.Account;
import com.digitalmoneyhouse.account.entity.Card;
import com.digitalmoneyhouse.account.entity.CardType;
import com.digitalmoneyhouse.account.exception.ConflictException;
import com.digitalmoneyhouse.account.exception.ResourceNotFoundException;
import com.digitalmoneyhouse.account.repository.AccountRepository;
import com.digitalmoneyhouse.account.repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private CardService cardService;

    @Test
    void createStoresACardWithoutAccountAndHidesCvvFromTheResponse() {
        when(cardRepository.findByNumber("4111111111111111")).thenReturn(Optional.empty());
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 3L));

        var response = cardService.create(cardRequest(null));

        assertEquals(3L, response.id());
        assertNull(response.accountId());
        assertEquals("4111111111111111", response.number());
        assertEquals(CardType.DEBIT, response.type());
    }

    @Test
    void associateRejectsACardLinkedToAnotherAccount() {
        when(accountRepository.findById(8L)).thenReturn(Optional.of(account()));
        Card card = new Card();
        card.setAccountId(99L);
        card.setNumber("4111111111111111");
        when(cardRepository.findById(3L)).thenReturn(Optional.of(card));

        assertThrows(ConflictException.class, () -> cardService.associate(8L, "ana@example.com", cardRequest(3L)));
    }

    @Test
    void listAndDeleteUseTheAccountOwner() {
        when(accountRepository.findById(8L)).thenReturn(Optional.of(account()));
        Card card = withId(new Card(), 3L);
        card.setAccountId(8L);
        card.setNumber("4111111111111111");
        card.setHolderName("Ana Perez");
        card.setExpirationDate("12/28");
        card.setType(CardType.CREDIT);
        when(cardRepository.findByAccountId(8L)).thenReturn(List.of(card));
        when(cardRepository.findByIdAndAccountId(3L, 8L)).thenReturn(Optional.of(card));

        assertEquals(1, cardService.list(8L, "ana@example.com").size());
        cardService.delete(8L, 3L, "ana@example.com");
        verify(cardRepository).delete(card);
    }

    @Test
    void getReturnsNotFoundWhenTheCardIsNotOnTheAccount() {
        when(accountRepository.findById(8L)).thenReturn(Optional.of(account()));
        when(cardRepository.findByIdAndAccountId(3L, 8L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cardService.get(8L, 3L, "ana@example.com"));
    }

    private CardRequest cardRequest(Long cardId) {
        return new CardRequest(cardId, "4111111111111111", "Ana Perez", "12/28", "123", CardType.DEBIT);
    }

    private Account account() {
        Account account = new Account();
        account.setUserId(4L);
        account.setOwnerEmail("ana@example.com");
        account.setCvu("1234567890123456789012");
        account.setAlias("sol.luna.rio");
        account.setBalance(new BigDecimal("0.00"));
        try {
            var field = Account.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(account, 8L);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
        return account;
    }

    private Card withId(Card card, Long id) {
        try {
            var field = Card.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(card, id);
            return card;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
