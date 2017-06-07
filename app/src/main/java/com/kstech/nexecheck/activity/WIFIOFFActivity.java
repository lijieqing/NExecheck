package com.kstech.nexecheck.activity;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.base.BaseActivity;

public class WIFIOFFActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifioff);
    }

    @Override
    public Activity getactivity() {
        return this;
    }
}
