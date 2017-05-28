package com.kstech.nexecheck.view.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import com.kstech.nexecheck.R;
import com.kstech.nexecheck.adapter.MyAdapter;
import com.kstech.nexecheck.adapter.RealTimeGridViewAdapter;
import com.kstech.nexecheck.base.BaseFragment;
import com.kstech.nexecheck.utils.Globals;
import com.kstech.nexecheck.view.widget.DividerItemDecoration;

/**
 * Created by lijie on 2017/5/24.
 */

public class HomeCheckEntityFragment extends BaseFragment {
    private RecyclerView recyclerView;
    public MyAdapter myAdapter;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.fragment_home_check_entity,null);
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_home_realtime);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),6);
        if (Globals.HomeRealtimeViews.size()>0){
            myAdapter = new MyAdapter ();
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL_LIST));
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.HORIZONTAL_LIST));
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setAdapter(myAdapter);
        }
        return view;
    }
}
