package com.mapsrahal.maps.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;

import com.mapsrahal.maps.model.User;


public interface UserDao {


    void insert(User user);


    void update(User user);


    void delete(User user);

}
