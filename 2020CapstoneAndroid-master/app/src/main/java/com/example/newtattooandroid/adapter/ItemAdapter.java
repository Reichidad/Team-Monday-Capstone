package com.example.newtattooandroid.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.newtattooandroid.MainActivity;
import com.example.newtattooandroid.R;
import com.example.newtattooandroid.activity.DetailActivity;
import com.example.newtattooandroid.model.MainItem;

import java.util.ArrayList;


/**
 * Created by jungwoon on 2017. 1. 18..
 */

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private Context context;
    private ArrayList<MainItem> items;
    private int lastPosition = -1;
    private RequestManager requestManager;
    private Intent detailIntent;
    private MainActivity mainActivity;

    public ItemAdapter(ArrayList<MainItem> items, Context context, RequestManager requestManager, MainActivity mainActivity) {
        this.items = items;
        this.context = context;
        this.requestManager = requestManager;
        detailIntent = new Intent(context, DetailActivity.class);
        this.mainActivity = mainActivity;
    }

    public void setItems(ArrayList<MainItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    // 뷰 바인딩 부분을 한번만 하도록, ViewHolder 패턴 의무화
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        ImageView cleanImageView;
        ImageButton btn_like;

        public ViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.image_view);
            textView = (TextView) view.findViewById(R.id.text_view);
            cleanImageView = (ImageView) view.findViewById(R.id.clean_image_view);
            btn_like = (ImageButton) view.findViewById(R.id.btn_like);
        }
    }

    // 새로운 뷰 생성
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cardview, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    // RecyclerView의 getView 부분을 담당하는 부분
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String url = items.get(position).getTattooUrl().get(0);

        if (url != null) {
            Glide.with(context).load(url)
                    .into(holder.imageView);
        }
//        setAnimation(holder.imageView, position);
        holder.imageView.setOnClickListener(v -> {
            //상세페이지로 정보 넘김
            detailIntent.putExtra("urls", items.get(position).getTattooUrl());
            detailIntent.putExtra("bigShape", items.get(position).getBigShape());
            detailIntent.putExtra("postId", items.get(position).getPostId());
            detailIntent.putExtra("tattooistId", items.get(position).getTattooistId());
            detailIntent.putExtra("title", items.get(position).getTitle());
            detailIntent.putExtra("description", items.get(position).getDescription());
            detailIntent.putExtra("price", items.get(position).getPrice());
            detailIntent.putExtra("avgCleanScore", items.get(position).getAvgCleanScore());
            detailIntent.putExtra("designUrl", items.get(position).getDesignUrl());
            context.startActivity(detailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        });

        holder.textView.setText(items.get(position).getTitle());

        if (items.get(position).getAvgCleanScore() < 3.0) {
            holder.cleanImageView.setVisibility(View.INVISIBLE);
        } else {
            holder.cleanImageView.setVisibility(View.VISIBLE);
        }

        holder.btn_like.setOnClickListener((View view) -> {
            mainActivity.addLikePosts(items.get(position));
            holder.btn_like.setImageResource(R.drawable.ic_like_filled);
            //TODO
            //button 색상 변경
        });
    }

    // Item 개수를 반환하는 부분
    @Override
    public int getItemCount() {
        return items.size();
    }

    // View가 나올때 Animation을 주는 부분
    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewToAnimate.setAnimation(animation);
            lastPosition = position;
        }
    }
}