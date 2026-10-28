package com.example.fixflow.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.fixflow.data.entity.FavoriteEntity;
import com.example.fixflow.data.entity.ShopEntity;

import java.util.List;

@Dao
public interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertFavorite(FavoriteEntity favorite);

    @Query("DELETE FROM favorites WHERE userId = :userId AND shopId = :shopId")
    void deleteFavorite(long userId, long shopId);

    @Query("SELECT COUNT(*) FROM favorites WHERE userId = :userId AND shopId = :shopId")
    LiveData<Integer> isFavorite(long userId, long shopId);

    @Query("SELECT COUNT(*) FROM favorites WHERE userId = :userId AND shopId = :shopId")
    int isFavoriteSync(long userId, long shopId);

    @Query("SELECT s.* FROM shops s INNER JOIN favorites f ON s.shopId = f.shopId WHERE f.userId = :userId ORDER BY f.createdAt DESC")
    LiveData<List<ShopEntity>> getFavoriteShopsForUser(long userId);
}
