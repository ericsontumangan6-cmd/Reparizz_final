package com.example.fixflow.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.fixflow.data.dao.FavoriteDao;
import com.example.fixflow.data.dao.PaymentDao;
import com.example.fixflow.data.dao.RepairEstimateDao;
import com.example.fixflow.data.dao.RepairJobDao;
import com.example.fixflow.data.dao.RepairStatusHistoryDao;
import com.example.fixflow.data.dao.RepairTestDao;
import com.example.fixflow.data.dao.ServiceReviewDao;
import com.example.fixflow.data.dao.ShopDao;
import com.example.fixflow.data.dao.SparePartDao;
import com.example.fixflow.data.dao.UserDao;
import com.example.fixflow.data.dao.WarrantyClaimDao;
import com.example.fixflow.data.dao.WarrantyDao;
import com.example.fixflow.data.entity.FavoriteEntity;
import com.example.fixflow.data.entity.Payment;
import com.example.fixflow.data.entity.RepairEstimate;
import com.example.fixflow.data.entity.RepairJob;
import com.example.fixflow.data.entity.RepairStatusHistory;
import com.example.fixflow.data.entity.RepairTest;
import com.example.fixflow.data.entity.ServiceReview;
import com.example.fixflow.data.entity.ShopEntity;
import com.example.fixflow.data.entity.SparePart;
import com.example.fixflow.data.entity.UserEntity;
import com.example.fixflow.data.entity.Warranty;
import com.example.fixflow.data.entity.WarrantyClaim;

@Database(
    entities = {
        RepairJob.class,
        RepairEstimate.class,
        RepairStatusHistory.class,
        RepairTest.class,
        Payment.class,
        Warranty.class,
        WarrantyClaim.class,
        UserEntity.class,
        ShopEntity.class,
        FavoriteEntity.class,
        SparePart.class,
        ServiceReview.class
    },
    version = 6,
    exportSchema = false
)
public abstract class FixFlowDatabase extends RoomDatabase {

    public abstract RepairJobDao repairJobDao();
    public abstract RepairEstimateDao repairEstimateDao();
    public abstract RepairStatusHistoryDao repairStatusHistoryDao();
    public abstract RepairTestDao repairTestDao();
    public abstract PaymentDao paymentDao();
    public abstract WarrantyDao warrantyDao();
    public abstract WarrantyClaimDao warrantyClaimDao();

    public abstract UserDao userDao();
    public abstract ShopDao shopDao();
    public abstract FavoriteDao favoriteDao();
    public abstract SparePartDao sparePartDao();
    public abstract ServiceReviewDao serviceReviewDao();

    private static volatile FixFlowDatabase INSTANCE;

    public static FixFlowDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (FixFlowDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            FixFlowDatabase.class,
                            "fixflow_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
