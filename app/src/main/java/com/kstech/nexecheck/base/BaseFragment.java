package com.kstech.nexecheck.base;

import android.app.Activity;
import android.app.Fragment;

/**
 * Created by lijie on 2017/5/27.
 */

public class BaseFragment extends Fragment {
    protected Activity activity;

    public void setActivity(Activity activity){
        this.activity = activity;
    }
}
