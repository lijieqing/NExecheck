package com.kstech.nexecheck.view.fragment;

/**
 * Created by lijie on 2017/6/1.
 */

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.kstech.nexecheck.R;

/**
 * 扩展自@{@link DoCheckFragment} 功能完全一致 只需复写 DoCheckFragment 中的下一项目 按钮的点击时间即可
 */
public class SingleCheckFragment extends DoCheckFragment{
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isSingle = true;
    }

    @Override
    public void initViewComp(View view) {
        super.initViewComp(view);
        singleCheckNextItemBtn.setText("退出测量");
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.singleCheckNextItemBtn){
            exitFragment();
        }
    }
}
