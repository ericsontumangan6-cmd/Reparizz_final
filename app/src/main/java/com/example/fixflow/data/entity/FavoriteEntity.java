package com.example.fixflow.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "favorites",
    foreignKeys = {
        @ForeignKey(
            entity = UserEntity.class,
            parentColumns = "userId",
            childColumns = "userId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = ShopEntity.class,
            parentColumns = "shopId",
            childColumns = "shopId",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {@Index("userId"), @Index("shopId"), @Index(value = {"userId", "shopId"}, unique = true)}
)
public class FavoriteEntity {

    @PrimaryKey(autoGenerate = true)
    public long favoriteId;

    public long userId;
    public long shopId;
    public long createdAt;

    public FavoriteEntity(long favoriteId, long userId, long shopId, long createdAt) {
        this.favoriteId = favoriteId;
        this.userId = userId;
        this.shopId = shopId;
        this.createdAt = createdAt != 0 ? createdAt : System.currentTimeMillis();
    }
}
