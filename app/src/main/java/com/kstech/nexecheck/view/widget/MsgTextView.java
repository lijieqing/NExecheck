package com.kstech.nexecheck.view.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kstech.nexecheck.R;

/**
 * Created by lijie on 2017/6/2.
 */

public class MsgTextView extends RelativeLayout implements RealTimeView.MSGListener{
    private Activity context;
    private TextView textView;
    public MsgTextView(Activity context) {
        super(context);
        this.context = context;
        initView();
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
                textView.setText(content);
            }
        });
    }
    private void initView(){
        //setWillNotDraw(false);
        View view = View.inflate(context, R.layout.msg_view,null);
        textView = (TextView) view.findViewById(R.id.tv_msg_view);
        this.addView(view);
    }
}
