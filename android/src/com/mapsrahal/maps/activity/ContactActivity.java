package com.mapsrahal.maps.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

import com.mapsrahal.maps.R;
import com.mapsrahal.maps.WebContainerDelegate;
import com.mapsrahal.maps.settings.CopyrightFragment;
import com.mapsrahal.util.Constants;
import com.mapsrahal.util.statistics.AlohaHelper;
import com.mapsrahal.util.statistics.Statistics;

public class ContactActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView imageView;

    private WebContainerDelegate mDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        imageView = findViewById(R.id.coyright);
        imageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        WebView myWebView = (WebView) findViewById(R.id.webview);
        myWebView.setVisibility(View.VISIBLE);
        myWebView.loadUrl(Constants.Url.COPYRIGHT);
        //getSettingsActivity().replaceFragment(CopyrightFragment.class,getString(R.string.copyright), null);
    }
}
