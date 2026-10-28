package com.example.fixflow.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "spare_parts")
public class SparePart {

    @PrimaryKey(autoGenerate = true)
    public long partId;

    @NonNull
    public String partName;

    @NonNull
    public String partCategory; // "Screen", "Battery", "Charging Port", "Storage/RAM", "Tools/Paste"

    public int quantityInStock;
    public double unitCost; // Unit price in PHP (₱)
    public int minStockAlert;

    public SparePart(long partId, @NonNull String partName, @NonNull String partCategory,
                     int quantityInStock, double unitCost, int minStockAlert) {
        this.partId = partId;
        this.partName = partName;
        this.partCategory = partCategory != null ? partCategory : "General";
        this.quantityInStock = quantityInStock;
        this.unitCost = unitCost;
        this.minStockAlert = minStockAlert > 0 ? minStockAlert : 3;
    }

    public boolean isLowStock() {
        return quantityInStock <= minStockAlert;
    }
}
