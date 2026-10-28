package com.example.fixflow.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.fixflow.data.entity.ShopEntity;

import java.util.List;

@Dao
public interface ShopDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertShop(ShopEntity shop);

    @Update
    void updateShop(ShopEntity shop);

    @Delete
    void deleteShop(ShopEntity shop);

    @Query("SELECT * FROM shops WHERE shopId = :shopId LIMIT 1")
    LiveData<ShopEntity> getShopById(long shopId);

    @Query("SELECT * FROM shops WHERE shopId = :shopId LIMIT 1")
    ShopEntity getShopByIdSync(long shopId);

    @Query("SELECT * FROM shops WHERE approvalStatus = 'APPROVED' ORDER BY shopName ASC")
    LiveData<List<ShopEntity>> getApprovedShops();

    @Query("SELECT * FROM shops WHERE approvalStatus = 'APPROVED' ORDER BY shopName ASC")
    List<ShopEntity> getApprovedShopsSync();

    @Query("SELECT * FROM shops WHERE approvalStatus = 'APPROVED' AND " +
           "(:category IS NULL OR :category = '' OR :category = 'All' OR category = :category) AND " +
           "(:query IS NULL OR :query = '' OR shopName LIKE '%' || :query || '%' OR servicesOffered LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') " +
           "ORDER BY shopName ASC")
    LiveData<List<ShopEntity>> searchApprovedShops(String query, String category);

    @Query("SELECT * FROM shops WHERE approvalStatus = 'PENDING_APPROVAL' ORDER BY createdAt DESC")
    LiveData<List<ShopEntity>> getPendingShops();

    @Query("SELECT * FROM shops WHERE approvalStatus = 'PENDING_APPROVAL' ORDER BY createdAt DESC")
    List<ShopEntity> getPendingShopsSync();

    @Query("SELECT * FROM shops ORDER BY createdAt DESC")
    LiveData<List<ShopEntity>> getAllShops();

    @Query("SELECT * FROM shops WHERE submittedByUserId = :userId ORDER BY createdAt DESC")
    LiveData<List<ShopEntity>> getShopsBySubmittedUser(long userId);

    @Query("SELECT COUNT(*) FROM shops WHERE approvalStatus = 'APPROVED'")
    LiveData<Integer> getApprovedCount();

    @Query("SELECT COUNT(*) FROM shops WHERE approvalStatus = 'PENDING_APPROVAL'")
    LiveData<Integer> getPendingCount();

    @Query("SELECT COUNT(*) FROM shops WHERE approvalStatus = 'REJECTED'")
    LiveData<Integer> getRejectedCount();

    @Query("SELECT COUNT(*) FROM shops")
    int getTotalShopCountSync();
}
