package com.mapsrahal.maps.model.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.mapsrahal.maps.model.User;
import com.mapsrahal.maps.model.dao.UserDao;

//@Database(entities = {User.class}, version = 1)
public abstract class UserDatabase extends RoomDatabase {

    private static UserDatabase instance;

    public abstract UserDao userDao();

    public static synchronized UserDatabase getInstance(Context context) {
        if(instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    UserDatabase.class,"user_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
