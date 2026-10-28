package com.example.fixflow.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.fixflow.data.entity.UserEntity;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertUser(UserEntity user);

    @Update
    void updateUser(UserEntity user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getUserByEmailSync(String email);

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    LiveData<UserEntity> getUserById(long userId);

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    UserEntity getUserByIdSync(long userId);

    @Query("SELECT * FROM users WHERE role = 'ADMIN' LIMIT 1")
    UserEntity getAnyAdminSync();

    @Query("SELECT COUNT(*) FROM users")
    int getUserCountSync();

    @Query("SELECT * FROM users WHERE role = :role ORDER BY fullName ASC")
    List<UserEntity> getUsersByRoleSync(String role);

    @Query("SELECT * FROM users WHERE role = 'STAFF' ORDER BY fullName ASC")
    LiveData<List<UserEntity>> getAllStaff();

    @Query("SELECT * FROM users WHERE role = 'STAFF' ORDER BY fullName ASC")
    List<UserEntity> getAllStaffSync();

    @Query("SELECT * FROM users WHERE role = 'CUSTOMER' ORDER BY fullName ASC")
    LiveData<List<UserEntity>> getAllCustomers();

    @Query("SELECT * FROM users WHERE role = 'CUSTOMER' ORDER BY fullName ASC")
    List<UserEntity> getAllCustomersSync();

    @Query("UPDATE users SET isActive = :isActive WHERE userId = :userId")
    void updateUserActiveStatus(long userId, boolean isActive);

    @Query("UPDATE users SET passwordHash = :newHash WHERE email = :email")
    void updatePasswordByEmail(String email, String newHash);

    @Query("UPDATE users SET passwordHash = :newHash WHERE userId = :userId")
    void updatePasswordByUserId(long userId, String newHash);
}
