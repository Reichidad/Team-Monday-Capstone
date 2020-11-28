package com.example.newtattooandroid.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.newtattooandroid.R;
import com.example.newtattooandroid.model.SliderItem;
import com.example.newtattooandroid.model.TattooReviewItem;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;

public class TattooReviewMoreAdapter extends RecyclerView.Adapter<TattooReviewMoreAdapter.AppViewHolder>{

    private ArrayList<TattooReviewItem> appReviewsList;
    private final Context mContext;

    public TattooReviewMoreAdapter(ArrayList<TattooReviewItem> appReviewsList, Context mContext) {
        this.appReviewsList = appReviewsList;
        this.mContext = mContext;
    }

    public void setAppReviewsList(ArrayList<TattooReviewItem> appReviewsList) {
        this.appReviewsList = appReviewsList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review_more, parent, false);
        return new TattooReviewMoreAdapter.AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        holder.userName.setText(appReviewsList.get(position).getNickName());
        holder.userReview.setText(appReviewsList.get(position).getDescription());
        holder.userReviewDate.setText(appReviewsList.get(position).getDate());
        holder.userReviewRating.setRating((float)appReviewsList.get(position).getCleanScore());

        //뷰페이저, 슬라이드 이미지

        String tattooUrl1 = appReviewsList.get(position).getTattooUrl1();
        String tattooUrl2 = appReviewsList.get(position).getTattooUrl2();
        String cleanUrl = appReviewsList.get(position).getCleanUrl();

        SliderAdapter sliderAdapter = new SliderAdapter(mContext);
        if(!tattooUrl1.equals("null")) sliderAdapter.addItem(new SliderItem(tattooUrl1));
        if(!tattooUrl2.equals("null")) sliderAdapter.addItem(new SliderItem(tattooUrl2));
        if(!cleanUrl.equals("null")) sliderAdapter.addItem(new SliderItem(cleanUrl));
        holder.userReviewImages.setSliderAdapter(sliderAdapter);
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
        final SliderView userReviewImages;

        AppViewHolder(View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.iv_user_image);
            userName = itemView.findViewById(R.id.tv_user_name);
            userReview = itemView.findViewById(R.id.tv_user_review);
            userReviewDate = itemView.findViewById(R.id.tv_user_review_date);
            userReviewRating = itemView.findViewById(R.id.rb_user_review);
            userReviewImages = itemView.findViewById(R.id.isv_more);
        }
    }
}