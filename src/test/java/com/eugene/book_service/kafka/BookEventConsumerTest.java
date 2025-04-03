package com.eugene.book_service.kafka;

import com.eugene.book_service.dto.event.ReviewDtoEvent;
import com.eugene.book_service.dto.event.UserDtoEvent;
import com.eugene.book_service.model.Book;
import com.eugene.book_service.repository.BookRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class BookEventConsumerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private BookRepository bookRepository;
    @InjectMocks
    private BookEventConsumer bookEventConsumer;

    @Test
    void handleUserDeletedEvent() throws JsonProcessingException {

        Set<Long> reviewIds = Set.of(1L, 2L, 3L);
        Set<Long> reviewIdsToDelete = Set.of(3L);
        Set<Long> reviewIdsAfterDelete = Set.of(1L, 2L);

        Book book1 = new Book("isbn1", "title1", "description1", "author1", "url1");
        book1.setReviewsIds(reviewIds);
        Book book2 = new Book("isbn2", "title2", "description2", "author2", "url2");
        book2.setReviewsIds(reviewIdsAfterDelete);

        UserDtoEvent userDtoEvent = new UserDtoEvent(KafkaEventType.USER_DELETED,
                reviewIdsToDelete);
        String json = objectMapper.writeValueAsString(userDtoEvent);

        given(bookRepository.findAll()).willReturn(List.of(book1, book2));

        bookEventConsumer.handleUserDeletedEvent(json);

        book1.setReviewsIds(reviewIdsAfterDelete);
        verify(bookRepository, times(1)).save(book1);
        verify(bookRepository, times(1)).save(book2);
    }

    @Test
    void handleReviewsCreatedEvent() throws JsonProcessingException {

        Set<Long> reviewIds = Set.of(1L, 2L, 3L);
        Set<Long> reviewIdsToDelete = Set.of(3L);
        Set<Long> reviewIdsAfterDelete = Set.of(1L, 2L);

        Book book1 = new Book("isbn1", "title1", "description1", "author1", "url1");
        book1.setReviewsIds(reviewIds);

        ReviewDtoEvent reviewDtoEvent = new ReviewDtoEvent(KafkaEventType.REVIEWS_CREATED, "user1",
                "isbn1", reviewIdsToDelete);
        String json = objectMapper.writeValueAsString(reviewDtoEvent);

        given(bookRepository.findById(reviewDtoEvent.getIsbn())).willReturn(Optional.of(book1));

        bookEventConsumer.handleReviewsCreatedEvent(json);

        book1.setReviewsIds(reviewIdsAfterDelete);
        verify(bookRepository, times(1)).save(book1);
    }

    @Test
    void handleReviewsDeletedEvent() throws JsonProcessingException {

        Set<Long> reviewIds = Set.of(1L, 2L, 3L);
        Set<Long> reviewIdsToDelete = Set.of(3L);
        Set<Long> reviewIdsAfterDelete = Set.of(1L, 2L);

        Book book1 = new Book("isbn1", "title1", "description1", "author1", "url1");
        book1.setReviewsIds(reviewIds);
        Book book2 = new Book("isbn2", "title2", "description2", "author2", "url2");
        book2.setReviewsIds(reviewIdsAfterDelete);

        ReviewDtoEvent reviewDtoEvent = new ReviewDtoEvent(KafkaEventType.REVIEWS_DELETED, "user1",
                "isbn1", reviewIdsToDelete);
        String json = objectMapper.writeValueAsString(reviewDtoEvent);

        given(bookRepository.findAll()).willReturn(List.of(book1, book2));

        bookEventConsumer.handleReviewsDeletedEvent(json);

        book1.setReviewsIds(reviewIdsAfterDelete);
        verify(bookRepository, times(1)).save(book1);
        verify(bookRepository, times(1)).save(book2);
        assertThat(book1.getReviewsIds()).isEqualTo(reviewIdsAfterDelete);
    }
}