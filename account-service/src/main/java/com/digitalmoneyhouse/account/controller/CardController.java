package com.digitalmoneyhouse.account.controller;

import com.digitalmoneyhouse.account.dto.CardRequest;
import com.digitalmoneyhouse.account.dto.CardResponse;
import com.digitalmoneyhouse.account.security.CurrentUser;
import com.digitalmoneyhouse.account.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cards")
public class CardController {

    private final CardService cardService;
    private final CurrentUser currentUser;

    public CardController(CardService cardService, CurrentUser currentUser) {
        this.cardService = cardService;
        this.currentUser = currentUser;
    }

    @PostMapping
    public ResponseEntity<CardResponse> create(@Valid @RequestBody CardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.create(request));
    }

    @GetMapping("/accounts/{accountId}/cards")
    public List<CardResponse> list(@PathVariable Long accountId) {
        return cardService.list(accountId, currentUser.email());
    }

    @GetMapping("/accounts/{accountId}/cards/{cardId}")
    public CardResponse get(@PathVariable Long accountId, @PathVariable Long cardId) {
        return cardService.get(accountId, cardId, currentUser.email());
    }

    @DeleteMapping("/accounts/{accountId}/cards/{cardId}")
    public Map<String, String> delete(@PathVariable Long accountId, @PathVariable Long cardId) {
        cardService.delete(accountId, cardId, currentUser.email());
        return Map.of("message", "Tarjeta eliminada");
    }
}
