package com.kstech.nexecheck.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.kstech.nexecheck.R;
import com.kstech.nexecheck.utils.Globals;

/**
 * Created by lenovo on 2016/10/13.
 */
public class MyCheckAdapter extends RecyclerView.Adapter {

    public MyCheckAdapter() {
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout fl;

        public ViewHolder(View root) {
            super(root);
            fl = (FrameLayout) root.findViewById(R.id.fl_gv_item);
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(View.inflate(parent.getContext(), R.layout.rv_item, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder vh = (ViewHolder) holder;
        FrameLayout parent = (FrameLayout) Globals.CheckItemRealtimeViews.get(position).getParent();
        if (parent != null){
            parent.removeView(Globals.CheckItemRealtimeViews.get(position));
        }
        vh.fl.removeAllViews();
        vh.fl.addView(Globals.CheckItemRealtimeViews.get(position));
    }

    @Override
    public int getItemCount() {
        return Globals.CheckItemRealtimeViews.size();
    }
}
