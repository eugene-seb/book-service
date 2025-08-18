package com.eugene.book_service.unit.kafka;

import com.eugene.book_service.dto.event.ReviewDtoEvent;
import com.eugene.book_service.dto.event.UserDtoEvent;
import com.eugene.book_service.kafka.BookEventConsumer;
import com.eugene.book_service.kafka.KafkaEventType;
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
class BookEventConsumerTest
{
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Set<Long> reviewIdsToDelete;
    private final Set<Long> reviewIdsAfterDelete;
    private final Book book1;
    private final Book book2;
    
    @Mock
    private BookRepository bookRepository;
    @InjectMocks
    private BookEventConsumer bookEventConsumer;
    
    public BookEventConsumerTest() {
        Set<Long> reviewIds = Set.of(1L,
                                     2L,
                                     3L);
        this.reviewIdsToDelete = Set.of(3L);
        this.reviewIdsAfterDelete = Set.of(1L,
                                           2L);
        
        this.book1 = new Book("isbn1",
                              "title1",
                              "description1",
                              "author1",
                              "url1");
        this.book1.setReviewsIds(reviewIds);
        this.book2 = new Book("isbn2",
                              "title2",
                              "description2",
                              "author2",
                              "url2");
        this.book2.setReviewsIds(this.reviewIdsAfterDelete);
    }
    
    @Test
    void handleUserEvent_userDeleted() throws JsonProcessingException {
        
        UserDtoEvent userDtoEvent = new UserDtoEvent(KafkaEventType.USER_DELETED,
                                                     this.reviewIdsToDelete);
        String json = this.objectMapper.writeValueAsString(userDtoEvent);
        
        given(this.bookRepository.findAll()).willReturn(List.of(this.book1,
                                                                this.book2));
        this.bookEventConsumer.handleUserEvents(json);
        
        this.book1.setReviewsIds(this.reviewIdsAfterDelete);
        verify(this.bookRepository,
               times(1)).save(this.book1);
        verify(this.bookRepository,
               times(1)).save(this.book2);
    }
    
    @Test
    void handleReviewsEvent_reviewCreated() throws JsonProcessingException {
        
        ReviewDtoEvent reviewDtoEvent = new ReviewDtoEvent(KafkaEventType.REVIEWS_CREATED,
                                                           "user1",
                                                           "isbn1",
                                                           this.reviewIdsToDelete);
        String json = this.objectMapper.writeValueAsString(reviewDtoEvent);
        
        given(this.bookRepository.findById(reviewDtoEvent.getIsbn())).willReturn(Optional.of(this.book1));
        
        this.bookEventConsumer.handleReviewsEvents(json);
        
        this.book1.setReviewsIds(this.reviewIdsAfterDelete);
        verify(this.bookRepository,
               times(1)).save(this.book1);
    }
    
    @Test
    void handleReviewsEvent_reviewDeleted() throws JsonProcessingException {
        
        ReviewDtoEvent reviewDtoEvent = new ReviewDtoEvent(KafkaEventType.REVIEWS_DELETED,
                                                           "user1",
                                                           "isbn1",
                                                           this.reviewIdsToDelete);
        String json = this.objectMapper.writeValueAsString(reviewDtoEvent);
        
        given(this.bookRepository.findAll()).willReturn(List.of(this.book1,
                                                                this.book2));
        
        this.bookEventConsumer.handleReviewsEvents(json);
        
        this.book1.setReviewsIds(this.reviewIdsAfterDelete);
        verify(this.bookRepository,
               times(1)).save(this.book1);
        verify(this.bookRepository,
               times(1)).save(this.book2);
        assertThat(this.book1.getReviewsIds()).isEqualTo(this.reviewIdsAfterDelete);
    }
}