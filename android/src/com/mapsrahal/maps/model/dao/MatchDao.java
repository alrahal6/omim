package com.mapsrahal.maps.model.dao;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.mapsrahal.maps.model.MatchingItem;
import com.mapsrahal.maps.model.User;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface MatchDao {

    @Insert
    void insert(MatchingItem matchingItem);

    @Update
    void update(MatchingItem matchingItem);

    @Delete
    void delete(MatchingItem matchingItem);

    /*
    @Query("SELECT * FROM playlist " +
    "WHERE playlist_title LIKE '% :playlistTitle %' " +
    "GROUP BY playlist_title " +
    "ORDER BY playlist_title " +
    "LIMIT :limit")
    List<IPlaylist> searchPlaylists(String playlistTitle, int limit);
     */
    @Query("SELECT * FROM match_table "+
            "LIMIT :limit OFFSET :offset")
    LiveData<MatchingItem> getMatchList(int limit, int offset);


    /*public static class UpdateMatchAsyncTask extends AsyncTask<Integer, Integer, Void> {
        private MatchDao matchDao;

        private UpdateMatchAsyncTask(MatchDao matchDao) {
            this.matchDao = matchDao;
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            this.getMatchList(integers[0],integers[1]);
            return null;
        }
    }*/

}
