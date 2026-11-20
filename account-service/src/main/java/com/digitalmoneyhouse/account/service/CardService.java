package com.digitalmoneyhouse.account.service;

import com.digitalmoneyhouse.account.dto.CardRequest;
import com.digitalmoneyhouse.account.dto.CardResponse;
import com.digitalmoneyhouse.account.entity.Account;
import com.digitalmoneyhouse.account.entity.Card;
import com.digitalmoneyhouse.account.exception.ConflictException;
import com.digitalmoneyhouse.account.exception.ForbiddenException;
import com.digitalmoneyhouse.account.exception.ResourceNotFoundException;
import com.digitalmoneyhouse.account.exception.ValidationException;
import com.digitalmoneyhouse.account.repository.AccountRepository;
import com.digitalmoneyhouse.account.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;

    public CardService(CardRepository cardRepository, AccountRepository accountRepository) {
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public CardResponse create(CardRequest request) {
        requireData(request);
        if (cardRepository.findByNumber(request.number()).isPresent()) {
            throw new ConflictException("La tarjeta ya está registrada");
        }
        return CardResponse.from(cardRepository.save(newCard(request, null)));
    }

    @Transactional
    public CardResponse associate(Long accountId, String email, CardRequest request) {
        Account account = ownedAccount(accountId, email);
        if (request.cardId() != null) {
            Card card = cardRepository.findById(request.cardId())
                    .orElseThrow(() -> new ResourceNotFoundException("La tarjeta no existe"));
            return CardResponse.from(link(account, card));
        }
        requireData(request);
        Card card = cardRepository.findByNumber(request.number()).orElseGet(() -> newCard(request, null));
        return CardResponse.from(link(account, card));
    }

    @Transactional(readOnly = true)
    public List<CardResponse> list(Long accountId, String email) {
        Account account = ownedAccount(accountId, email);
        return cardRepository.findByAccountId(account.getId()).stream().map(CardResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CardResponse get(Long accountId, Long cardId, String email) {
        ownedAccount(accountId, email);
        return CardResponse.from(cardOnAccount(accountId, cardId));
    }

    @Transactional
    public void delete(Long accountId, Long cardId, String email) {
        ownedAccount(accountId, email);
        cardRepository.delete(cardOnAccount(accountId, cardId));
    }

    private Card link(Account account, Card card) {
        if (card.getAccountId() != null && !card.getAccountId().equals(account.getId())) {
            throw new ConflictException("La tarjeta está asociada a otra cuenta");
        }
        card.setAccountId(account.getId());
        return cardRepository.save(card);
    }

    private Card cardOnAccount(Long accountId, Long cardId) {
        return cardRepository.findByIdAndAccountId(cardId, accountId)
                .orElseThrow(() -> new ResourceNotFoundException("La tarjeta no existe"));
    }

    private Account ownedAccount(Long accountId, String email) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("La cuenta no existe"));
        if (!account.getOwnerEmail().equalsIgnoreCase(email)) {
            throw new ForbiddenException("No tenés permiso para ver esta cuenta");
        }
        return account;
    }

    private void requireData(CardRequest request) {
        if (request.number() == null || request.holderName() == null || request.holderName().isBlank()
                || request.expirationDate() == null || request.cvv() == null || request.type() == null) {
            throw new ValidationException("Faltan datos de la tarjeta");
        }
    }

    private Card newCard(CardRequest request, Long accountId) {
        Card card = new Card();
        card.setAccountId(accountId);
        card.setNumber(request.number());
        card.setHolderName(request.holderName().trim());
        card.setExpirationDate(request.expirationDate());
        card.setCvv(request.cvv());
        card.setType(request.type());
        return card;
    }
}
