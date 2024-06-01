package com.lunark.lunark.reviews.dto;

import com.lunark.lunark.reviews.model.Review;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {

    private int rating;
    private String description;
    private LocalDateTime date;
    private String author;
    private UUID authorId;
    private Review.ReviewType type;
    private Long id;

    public ReviewDto(int rating, String description, LocalDateTime date, String s, Long id, Long id1) {
    }

    public Review toReview(String reviewType){
        return new Review(null,
                this.rating,
                this.description,
                false,
                this.date,
                Review.ReviewType.valueOf(reviewType.toUpperCase()));
    }
}
