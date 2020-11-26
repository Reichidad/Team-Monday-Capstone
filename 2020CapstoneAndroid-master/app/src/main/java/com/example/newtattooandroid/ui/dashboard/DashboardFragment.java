package com.example.newtattooandroid.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.newtattooandroid.MainActivity;
import com.example.newtattooandroid.R;
import com.example.newtattooandroid.adapter.LikeItemAdapter;
import com.example.newtattooandroid.model.MainItem;

import java.util.ArrayList;

public class DashboardFragment extends Fragment {
    private DashboardViewModel dashboardViewModel;

    private RecyclerView mRecyclerView;
    private LikeItemAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RequestManager requestManager;


    private ArrayList<MainItem> likePosts;
    private MainActivity mainActivity;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mainActivity = (MainActivity) getActivity();
        likePosts = mainActivity.getLikePosts();

        mRecyclerView = (RecyclerView) root.findViewById(R.id.rv_like_tattoo);

        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        requestManager = Glide.with(this);
        mAdapter = new LikeItemAdapter(likePosts, getContext(), requestManager);
        mRecyclerView.setAdapter(mAdapter);

        getLikePosts();

        return root;
    }

    private void getLikePosts(){
        likePosts = mainActivity.getLikePosts();
        mAdapter = new LikeItemAdapter(likePosts, getContext(), requestManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }
}