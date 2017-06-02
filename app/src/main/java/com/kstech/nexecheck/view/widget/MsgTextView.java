package com.kstech.nexecheck.view.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Created by lijie on 2017/6/2.
 */

public class MsgTextView extends android.support.v7.widget.AppCompatTextView implements RealTimeView.MSGListener{
    private Activity context;
    public MsgTextView(Activity context) {
        super(context);
        setWillNotDraw(false);
        this.context = context;
        this.setText("^——^");
        this.setTextColor(Color.BLACK);
    }

    public MsgTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MsgTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onMsgError(final String content) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMsg().setText(content);
            }
        });
    }
    public MsgTextView getMsg(){
        return this;
    }
}
