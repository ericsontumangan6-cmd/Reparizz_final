package com.example.fixflow.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "users",
    indices = {@Index(value = {"email"}, unique = true)}
)
public class UserEntity {

    @PrimaryKey(autoGenerate = true)
    public long userId;

    @NonNull
    public String fullName;

    @NonNull
    public String email;

    @NonNull
    public String passwordHash;

    @NonNull
    public String role; // "ADMIN", "STAFF", "CUSTOMER"

    public boolean isActive;

    public long createdAt;

    public UserEntity(long userId, @NonNull String fullName, @NonNull String email,
                      @NonNull String passwordHash, @NonNull String role, boolean isActive, long createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email.toLowerCase().trim();
        this.passwordHash = passwordHash;
        this.role = role != null ? role.toUpperCase().trim() : "CUSTOMER";
        this.isActive = isActive;
        this.createdAt = createdAt != 0 ? createdAt : System.currentTimeMillis();
    }
}
