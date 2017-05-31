package com.kstech.nexecheck.base;

/**
 * @author lijie on 2017.05.31
 */
public interface NetWorkStatusListener {
    /**
     *  监听网络状态 改变主界面 图表状态变化On status changed.
     *
     * @param off the off
     */
    void onStatusChanged(boolean off);
}