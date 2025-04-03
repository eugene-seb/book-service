package com.eugene.book_service.kafka;

import com.eugene.book_service.dto.event.BookDtoEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class BookEventProducerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    @InjectMocks
    private BookEventProducer bookEventProducer;

    @Test
    void sendBookDeletedEvent() throws JsonProcessingException {

        Set<Long> reviewsIds = Set.of(1L, 2L);
        String json = objectMapper.writeValueAsString(
                new BookDtoEvent(KafkaEventType.BOOK_DELETED, reviewsIds));

        bookEventProducer.sendBookDeletedEvent(reviewsIds);

        verify(kafkaTemplate, times(1)).send("book.events", json);
    }
}
