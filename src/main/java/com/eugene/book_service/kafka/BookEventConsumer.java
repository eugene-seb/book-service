package com.eugene.book_service.kafka;

import com.eugene.book_service.dto.event.ReviewDtoEvent;
import com.eugene.book_service.dto.event.UserDtoEvent;
import com.eugene.book_service.model.Book;
import com.eugene.book_service.repository.BookRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookEventConsumer {

    private final Logger log = LoggerFactory.getLogger(BookEventConsumer.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BookRepository bookRepository;

    public BookEventConsumer(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @KafkaListener(topics = "user.events", groupId = "book-service-group")
    @Transactional
    public void handleUserDeletedEvent(String json) throws JsonProcessingException {
        UserDtoEvent userDtoEvent = objectMapper.readValue(json, UserDtoEvent.class);
        if ("USER_DELETED".equals(userDtoEvent.getEventType())) {
            deleteBookReviewsByIds(userDtoEvent.getReviewsIds());
        }
    }

    @KafkaListener(topics = "review.events", groupId = "book-service-group")
    @Transactional
    public void handleReviewsCreatedEvent(String json) throws JsonProcessingException {
        ReviewDtoEvent reviewDtoEvent = objectMapper.readValue(json, ReviewDtoEvent.class);
        if ("REVIEWS_CREATED".equals(reviewDtoEvent.getEventType())) {
            bookRepository
                    .findById(reviewDtoEvent.getIsbn())
                    .ifPresent(book -> {
                        book
                                .getReviewsIds()
                                .addAll(reviewDtoEvent.getReviewsIds());
                        bookRepository.save(book);
                        log.info("ID of the review saved in Book");
                    });
        }
    }

    @KafkaListener(topics = "review.events", groupId = "book-service-group")
    @Transactional
    public void handleReviewsDeletedEvent(String json) throws JsonProcessingException {
        ReviewDtoEvent reviewDtoEvent = objectMapper.readValue(json, ReviewDtoEvent.class);
        if ("REVIEWS_DELETED".equals(reviewDtoEvent.getEventType())) {
            deleteBookReviewsByIds(reviewDtoEvent.getReviewsIds());
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
        log.info("Reviews deleted in Books");
    }
}