package com.thechoicecompany.service;

import com.thechoicecompany.dto.request.NewsletterRequest;
import com.thechoicecompany.entity.NewsletterSubscriber;
import com.thechoicecompany.exception.DuplicateResourceException;
import com.thechoicecompany.repository.NewsletterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NewsletterService {

    private final NewsletterRepository newsletterRepository;

    @Transactional
    public void subscribe(NewsletterRequest request) {
        if (newsletterRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already subscribed: " + request.getEmail());
        }
        NewsletterSubscriber subscriber = NewsletterSubscriber.builder()
            .email(request.getEmail())
            .source(request.getSource())
            .build();
        newsletterRepository.save(subscriber);
    }
}
