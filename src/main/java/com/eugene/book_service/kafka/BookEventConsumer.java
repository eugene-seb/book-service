package com.eugene.book_service.kafka;

import com.eugene.book_service.dto.event.ReviewEvent;
import com.eugene.book_service.dto.event.UserEvent;
import com.eugene.book_service.model.Book;
import com.eugene.book_service.repository.BookRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookEventConsumer {

    private final BookRepository bookRepository;

    public BookEventConsumer(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @KafkaListener(topics = "user.events", groupId = "book-service-group")
    @Transactional
    public void handleUserDeletedEvent(UserEvent event) {
        if ("USER_DELETED".equals(event.eventType())) {
            deleteBookReviewsByIds(event.reviewsIds());
        }
    }

    @KafkaListener(topics = "review.events", groupId = "book-service-group")
    @Transactional
    public void handleReviewsCreatedEvent(ReviewEvent event) {
        if ("REVIEWS_CREATED".equals(event.eventType())) {
            bookRepository
                    .findById(event.isbn())
                    .ifPresent(book -> {
                        book
                                .getReviewsIds()
                                .addAll(event.reviewsIds());
                        bookRepository.save(book);
                    });
        }
    }

    @KafkaListener(topics = "review.events", groupId = "book-service-group")
    @Transactional
    public void handleReviewsDeletedEvent(ReviewEvent event) {
        if ("REVIEWS_DELETED".equals(event.eventType())) {
            deleteBookReviewsByIds(event.reviewsIds());
        }
    }

    private void deleteBookReviewsByIds(Set<Long> reviewsIds) {
        List<Book> books = bookRepository.findAll();
        for (Book b : books) {
            Set<Long> reviewsIdsUpdated = b
                    .getReviewsIds()
                    .stream()
                    .filter(r -> !reviewsIds.contains(r))
                    .collect(Collectors.toSet());
            b.setReviewsIds(reviewsIdsUpdated);
            bookRepository.save(b);
        }
    }
}