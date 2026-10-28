package com.example.fixflow.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "shops",
    indices = {@Index("category"), @Index("approvalStatus")}
)
public class ShopEntity {

    @PrimaryKey(autoGenerate = true)
    public long shopId;

    @NonNull
    public String shopName;

    @NonNull
    public String category; // "Cleaning", "Plumbing", "Electronics"

    @NonNull
    public String address; // Address in Malolos, Bulacan

    @NonNull
    public String contactNumber;

    @Nullable
    public String description;

    @Nullable
    public String servicesOffered;

    @Nullable
    public String operatingHours;

    public double latitude;
    public double longitude;

    @NonNull
    public String approvalStatus; // "PENDING_APPROVAL", "APPROVED", "REJECTED"

    @Nullable
    public String rejectionReason;

    public long submittedByUserId;
    public boolean isDemoData;
    public long createdAt;

    public ShopEntity(long shopId, @NonNull String shopName, @NonNull String category,
                      @NonNull String address, @NonNull String contactNumber,
                      @Nullable String description, @Nullable String servicesOffered,
                      @Nullable String operatingHours, double latitude, double longitude,
                      @NonNull String approvalStatus, @Nullable String rejectionReason,
                      long submittedByUserId, boolean isDemoData, long createdAt) {
        this.shopId = shopId;
        this.shopName = shopName;
        this.category = category;
        this.address = address;
        this.contactNumber = contactNumber;
        this.description = description;
        this.servicesOffered = servicesOffered;
        this.operatingHours = operatingHours;
        this.latitude = latitude;
        this.longitude = longitude;
        this.approvalStatus = approvalStatus != null ? approvalStatus : "APPROVED";
        this.rejectionReason = rejectionReason;
        this.submittedByUserId = submittedByUserId;
        this.isDemoData = isDemoData;
        this.createdAt = createdAt != 0 ? createdAt : System.currentTimeMillis();
    }
}
