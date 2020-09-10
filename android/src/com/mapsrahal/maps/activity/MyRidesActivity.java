package com.mapsrahal.maps.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import com.mapsrahal.maps.MySharedPreference;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.activity.ui.main.SectionsPagerAdapter;
import com.mapsrahal.maps.base.BaseMwmFragmentActivity;

public class MyRidesActivity extends BaseMwmFragmentActivity {


    private ImageButton mMainMenu;
    @Override
    protected void onSafeCreate(Bundle savedInstanceState) {
        super.onSafeCreate(savedInstanceState);
        setContentView(R.layout.activity_my_rides);
        displayToolbarAsActionBar();
        //getToolbar().setTitle("My Trips");
        getSupportActionBar().setTitle("My Trips");
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        /*mDrawerLayout = findViewById(R.id.rides_drawer_layout);
        mMainMenu = findViewById(R.id.mainMenu);
        mMainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        mpPhone = headerView.findViewById(R.id.pPhone);
        mpPhone.setText(MySharedPreference.getInstance(getApplicationContext()).getPhoneNumber());
        //mDrawerLayout = findViewById(R.id.rides_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, getToolbar(), R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        mDrawerLayout.setBackgroundResource(R.color.white_60);
        toggle.syncState();*/
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.my_view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.my_tabs);
        tabs.setupWithViewPager(viewPager);
    }

    /*@Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        super.onNavigationItemSelected(item);
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }*/

    /*@Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }*/
}