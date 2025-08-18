package com.eugene.book_service.kafka;

import com.eugene.book_service.dto.event.ReviewDtoEvent;
import com.eugene.book_service.dto.event.UserDtoEvent;
import com.eugene.book_service.model.Book;
import com.eugene.book_service.repository.BookRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookEventConsumer
{
    private final Logger log = LoggerFactory.getLogger(BookEventConsumer.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BookRepository bookRepository;
    
    @KafkaListener(topics = "user.events", groupId = "book-service-group")
    @Transactional
    public void handleUserEvents(String json) throws JsonProcessingException {
        UserDtoEvent userDtoEvent = this.objectMapper.readValue(json,
                                                                UserDtoEvent.class);
        if (Objects.equals(userDtoEvent.getEventType(),
                           KafkaEventType.USER_DELETED)) {
            deleteBookReviewsByIds(userDtoEvent.getReviewsIds());
        }
    }
    
    @KafkaListener(topics = "review.events", groupId = "book-service-group")
    @Transactional
    public void handleReviewsEvents(String json) throws JsonProcessingException {
        ReviewDtoEvent reviewDtoEvent = this.objectMapper.readValue(json,
                                                                    ReviewDtoEvent.class);
        switch (reviewDtoEvent.getEventType()) {
            case REVIEWS_CREATED -> addNewReviewsToBook(reviewDtoEvent);
            case REVIEWS_DELETED -> deleteBookReviewsByIds(reviewDtoEvent.getReviewsIds());
            case null, default -> {
                // No need to treat the other enum values since user-service don't listen those events in review topic
            }
        }
    }
    
    private void addNewReviewsToBook(ReviewDtoEvent reviewDtoEvent) {
        bookRepository
                .findById(reviewDtoEvent.getIsbn())
                .ifPresent(book -> {
                    Set<Long> reviewIds = new HashSet<>(book.getReviewsIds());
                    reviewIds.addAll(reviewDtoEvent.getReviewsIds());
                    book.setReviewsIds(reviewIds);
                    bookRepository.save(book);
                    this.log.info("New review added to the book.");
                });
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
        this.log.info("Reviews deleted in Books");
    }
}