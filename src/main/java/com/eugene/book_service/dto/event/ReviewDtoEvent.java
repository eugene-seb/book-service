package com.eugene.book_service.dto.event;

import com.eugene.book_service.kafka.KafkaEventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
public class ReviewDtoEvent
        extends BaseDtoEvent
{
    
    private String userId;
    private String isbn;
    private Set<Long> reviewsIds;
    
    public ReviewDtoEvent(
            KafkaEventType eventType,
            String userId,
            String isbn,
            Set<Long> reviewsIds
    ) {
        super(eventType);
        this.userId = userId;
        this.isbn = isbn;
        this.reviewsIds = reviewsIds;
    }
}
