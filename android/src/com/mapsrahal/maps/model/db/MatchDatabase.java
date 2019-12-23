package com.mapsrahal.maps.model.db;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.mapsrahal.maps.model.MatchingItem;
import com.mapsrahal.maps.model.User;
import com.mapsrahal.maps.model.dao.MatchDao;
import com.mapsrahal.maps.model.dao.UserDao;

@Database(entities = MatchingItem.class, version = 1, exportSchema = false)
public abstract class MatchDatabase extends RoomDatabase {

    private static MatchDatabase instance;

    public abstract MatchDao matchDao();

    public static synchronized MatchDatabase getInstance(Context context) {
        if(instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    MatchDatabase.class,"match_table")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }
    };

    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private MatchDao matchDao;

        private PopulateDbAsyncTask(MatchDatabase db) {
            matchDao = db.matchDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //noteDao.insert(new Note("Title 1", "Description 1", 1));
            //noteDao.insert(new Note("Title 2", "Description 2", 2));
            //noteDao.insert(new Note("Title 3", "Description 3", 3));
            return null;
        }
    }
}
