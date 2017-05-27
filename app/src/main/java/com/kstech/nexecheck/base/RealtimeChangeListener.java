package com.kstech.nexecheck.base;

/**
 * Created by lijie on 2017/5/25.
 */

public interface RealtimeChangeListener {
    void onDataChanged(float value);//接收到数据后通知
}
