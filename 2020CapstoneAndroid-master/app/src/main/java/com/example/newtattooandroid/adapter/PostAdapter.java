package com.example.newtattooandroid.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.newtattooandroid.R;
import com.example.newtattooandroid.activity.DetailActivity;
import com.example.newtattooandroid.model.MainItem;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private Context context;
    private ArrayList<MainItem> posts;
    private Intent detailIntent;

    public PostAdapter(ArrayList<MainItem> posts, Context context) {
        this.posts = posts;
        this.context = context;
        detailIntent = new Intent(context, DetailActivity.class);
    }

    public void setItems(ArrayList<MainItem> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    // 뷰 바인딩 부분을 한번만 하도록, ViewHolder 패턴 의무화
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_tattooist_post;
        LinearLayout ly_tattooist_post;
        public ViewHolder(View view) {
            super(view);
            iv_tattooist_post = (ImageView) view.findViewById(R.id.iv_tattooist_post);
            ly_tattooist_post = (LinearLayout) view.findViewById(R.id.ly_tattooist_post);
        }
    }

    // 새로운 뷰 생성
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tattooist_post, parent, false);
        PostAdapter.ViewHolder viewHolder = new PostAdapter.ViewHolder(view);

        return viewHolder;
    }

    // RecyclerView의 getView 부분을 담당하는 부분
    @Override
    public void onBindViewHolder(PostAdapter.ViewHolder holder, int position) {
        String url = posts.get(position).getTattooUrl().get(0);
        if (url != null) {
            Glide.with(context).load(url)
                    .into(holder.iv_tattooist_post);
        }

        holder.ly_tattooist_post.setOnClickListener((View view) -> {
            detailIntent.putExtra("urls", posts.get(position).getTattooUrl());
            detailIntent.putExtra("bigShape", posts.get(position).getBigShape());
            detailIntent.putExtra("postId", posts.get(position).getPostId());
            detailIntent.putExtra("tattooistId", posts.get(position).getTattooistId());
            detailIntent.putExtra("title", posts.get(position).getTitle());
            detailIntent.putExtra("description", posts.get(position).getDescription());
            detailIntent.putExtra("price", posts.get(position).getPrice());
            detailIntent.putExtra("avgCleanScore", posts.get(position).getAvgCleanScore());
            detailIntent.putExtra("designUrl", posts.get(position).getDesignUrl());
            context.startActivity(detailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        });
    }

    // Item 개수를 반환하는 부분
    @Override
    public int getItemCount() {
        return posts.size();
    }

}
