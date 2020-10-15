package com.mapsrahal.maps.activity.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.mapsrahal.maps.R;
import com.mapsrahal.util.UiUtils;

public class BlockActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiUtils.setupColorStatusBar(this, R.color.bg_statusbar);
        setContentView(R.layout.activity_block);
        /*if(isHaveInternet()) {
            setContentView(R.layout.activity_no_internet);
        } else {
            setContentView(R.layout.activity_block);
        }*/
    }

    /*private boolean isHaveInternet() {
        return true;
    }*/
}
