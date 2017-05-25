package com.kstech.nexecheck.activity;

import android.app.Activity;
import android.os.Bundle;

import com.kstech.nexecheck.BaseActivity;
import com.kstech.nexecheck.R;

public class HomeActivity extends BaseActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initMenu("");
    }

    @Override
    public Activity getactivity() {
        return this;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


}
