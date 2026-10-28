package com.example.fixflow.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.fixflow.data.entity.ServiceReview;

import java.util.List;

@Dao
public interface ServiceReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertReview(ServiceReview review);

    @Query("SELECT * FROM service_reviews WHERE jobId = :jobId LIMIT 1")
    LiveData<ServiceReview> getReviewForJob(String jobId);

    @Query("SELECT * FROM service_reviews WHERE jobId = :jobId LIMIT 1")
    ServiceReview getReviewForJobSync(String jobId);

    @Query("SELECT * FROM service_reviews ORDER BY reviewTimestamp DESC")
    LiveData<List<ServiceReview>> getAllReviews();

    @Query("SELECT AVG(ratingStars) FROM service_reviews")
    LiveData<Double> getAverageRating();
}
