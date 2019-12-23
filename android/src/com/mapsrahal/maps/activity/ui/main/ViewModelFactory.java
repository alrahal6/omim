package com.mapsrahal.maps.activity.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.HashMap;
import java.util.Map;

public class ViewModelFactory implements ViewModelProvider.Factory {

    HashMap hashMapViewModel = new HashMap<String, ViewModel>();

    @NonNull
    private final Application application;

    //private final long id;

    public ViewModelFactory(@NonNull Application application) {
        this.application = application;
        //this.id = id;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass == PageViewModel.class) {
            return (T) new PageViewModel(application);
        }
        return null;


        /*if (modelClass.isAssignableFrom(PageViewModel.class)) {
            String key = "UserProfileViewModel";
            if(hashMapViewModel.containsKey(key)){
                return (T) new getViewModel(key);
            } else {
                addViewModel(key, UserProfileViewModel())
                return getViewModel(key) as T
            }
        }
        return null;*/
    }

    /*public void addViewModel(String key,ViewModel viewModel){
        hashMapViewModel.put(key, viewModel);
    }
    public ViewModel getViewModel(String key) {
        return hashMapViewModel.get(key);
        //return hashMapViewModel[key];
    }*/
}
