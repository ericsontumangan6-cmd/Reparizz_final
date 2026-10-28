package com.example.fixflow.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "service_reviews",
    foreignKeys = @ForeignKey(
        entity = RepairJob.class,
        parentColumns = "jobId",
        childColumns = "jobId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("jobId")}
)
public class ServiceReview {

    @PrimaryKey(autoGenerate = true)
    public long reviewId;

    @NonNull
    public String jobId;

    public int ratingStars; // 1 to 5 stars

    @Nullable
    public String feedbackComment;

    public long reviewTimestamp;

    public ServiceReview(long reviewId, @NonNull String jobId, int ratingStars,
                         @Nullable String feedbackComment, long reviewTimestamp) {
        this.reviewId = reviewId;
        this.jobId = jobId;
        this.ratingStars = ratingStars > 0 ? Math.min(ratingStars, 5) : 5;
        this.feedbackComment = feedbackComment;
        this.reviewTimestamp = reviewTimestamp != 0 ? reviewTimestamp : System.currentTimeMillis();
    }
}
