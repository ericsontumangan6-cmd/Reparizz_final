package com.example.fixflow.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.fixflow.data.entity.SparePart;

import java.util.List;

@Dao
public interface SparePartDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPart(SparePart part);

    @Update
    void updatePart(SparePart part);

    @Query("SELECT * FROM spare_parts ORDER BY partName ASC")
    LiveData<List<SparePart>> getAllParts();

    @Query("SELECT * FROM spare_parts ORDER BY partName ASC")
    List<SparePart> getAllPartsSync();

    @Query("SELECT * FROM spare_parts WHERE partId = :partId LIMIT 1")
    SparePart getPartByIdSync(long partId);

    @Query("UPDATE spare_parts SET quantityInStock = quantityInStock - :qty WHERE partId = :partId AND quantityInStock >= :qty")
    int deductStock(long partId, int qty);
}
