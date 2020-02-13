package com.mapsrahal.maps.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.mapsrahal.maps.R;
import com.mapsrahal.maps.WebContainerDelegate;
import com.mapsrahal.util.Constants;
import com.mapsrahal.util.UiUtils;

public class ContactActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textView;

    private WebContainerDelegate mDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        textView = findViewById(R.id.coyright);
        textView.setOnClickListener(this);
        UiUtils.setupColorStatusBar(this, R.color.bg_statusbar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public void onClick(View view) {
        WebView myWebView = (WebView) findViewById(R.id.webview);
        myWebView.setVisibility(View.VISIBLE);
        myWebView.loadUrl(Constants.Url.COPYRIGHT);
        //getSettingsActivity().replaceFragment(CopyrightFragment.class,getString(R.string.copyright), null);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
