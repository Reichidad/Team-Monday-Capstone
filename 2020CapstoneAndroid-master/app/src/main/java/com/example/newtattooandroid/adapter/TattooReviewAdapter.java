package com.example.newtattooandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtattooandroid.R;
import com.example.newtattooandroid.model.TattooReviewItem;

import java.util.ArrayList;

public class TattooReviewAdapter extends RecyclerView.Adapter<TattooReviewAdapter.AppViewHolder> {

    private ArrayList<TattooReviewItem> appReviewsList;
    private final Context mContext;

    public TattooReviewAdapter(ArrayList<TattooReviewItem> appReviewsList, Context context) {
        this.appReviewsList = appReviewsList;
        this.mContext = context;

    }

    public void setAppReviewsList(ArrayList<TattooReviewItem> appReviewsList) {
        this.appReviewsList = appReviewsList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TattooReviewAdapter.AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tattoo_review, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TattooReviewAdapter.AppViewHolder holder, int position) {
        holder.userName.setText(appReviewsList.get(position).getNickName());
        holder.userReview.setText(appReviewsList.get(position).getDescription());
        holder.userReviewDate.setText(appReviewsList.get(position).getDate());
        holder.userReviewRating.setRating((float)appReviewsList.get(position).getCleanScore());



    }

    @Override
    public int getItemCount() {
        return appReviewsList.size();
    }

    class AppViewHolder extends RecyclerView.ViewHolder {

        final ImageView userImage;
        final TextView userName;
        final TextView userReview;
        final TextView userReviewDate;
        final RatingBar userReviewRating;

        AppViewHolder(View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.iv_user_image);
            userName = itemView.findViewById(R.id.tv_user_name);
            userReview = itemView.findViewById(R.id.tv_user_review);
            userReviewDate = itemView.findViewById(R.id.tv_user_review_date);
            userReviewRating = itemView.findViewById(R.id.rb_user_review);
        }
    }
}

