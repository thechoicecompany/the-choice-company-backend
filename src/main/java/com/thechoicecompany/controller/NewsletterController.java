package com.thechoicecompany.controller;

import com.thechoicecompany.dto.request.NewsletterRequest;
import com.thechoicecompany.service.NewsletterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/newsletter")
@RequiredArgsConstructor
public class NewsletterController {

    private final NewsletterService newsletterService;

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(@Valid @RequestBody NewsletterRequest request) {
        newsletterService.subscribe(request);
        return ResponseEntity.ok().build();
    }
}
