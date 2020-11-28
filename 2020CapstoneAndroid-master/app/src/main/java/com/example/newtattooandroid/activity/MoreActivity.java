package com.example.newtattooandroid.activity;

import android.content.Intent;
import android.util.Log;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.newtattooandroid.R;
import com.example.newtattooandroid.adapter.TattooReviewAdapter;
import com.example.newtattooandroid.adapter.TattooReviewMoreAdapter;
import com.example.newtattooandroid.model.TattooReviewItem;
import com.example.newtattooandroid.network.NetworkAPIs;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.util.ArrayList;
import java.util.List;

public class MoreActivity extends AppCompatActivity {

    private RecyclerView tattooReviewMoreRecyclerView;
    private ArrayList<TattooReviewItem> appReviews;
    private TattooReviewMoreAdapter tmAdapter;

    // ui components
    private ImageButton btn_review_posts_back;
    private ImageButton btn_review_write;
    private TextView tv_review_title;

    //통신
    private Retrofit retrofit;
    private NetworkAPIs networkAPIs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);

        //기본 툴바 숨기기
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //뒤로가기 버튼 - 클릭 시 종료 이벤트 처리
        btn_review_posts_back = findViewById(R.id.btn_review_post_back);
        btn_review_posts_back.setOnClickListener((View view) -> {
            finish();
        });

        //리뷰 작성 버튼 - 클릭 시 리뷰 작성 form으로 이동
        btn_review_write = findViewById(R.id.btn_review_write);
        btn_review_write.setOnClickListener((View view) -> { // 글 작성
            //Todo : review 작성 Activity로 넘어가야 됨. 내 아이디 받아와야됨.
            Intent writePostIntent = new Intent(getApplicationContext(), WriteReviewActivity.class);
//            writePostIntent.putExtra("tattooistId", tattooistDto.getUserId());
            getApplicationContext().startActivity(writePostIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        });




        //타투리뷰 더보기 리사이클러뷰 세팅
        tattooReviewMoreRecyclerView = findViewById(R.id.rv_reviews_more);
        RecyclerView.LayoutManager verticalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        tattooReviewMoreRecyclerView.setLayoutManager(verticalLayoutManager);
        tattooReviewMoreRecyclerView.setHasFixedSize(true);
        //리뷰데이터 Intent로 전달받기
        appReviews = (ArrayList<TattooReviewItem>) getIntent().getSerializableExtra("appReviews");
        tmAdapter = new TattooReviewMoreAdapter(appReviews, getApplicationContext());
        tattooReviewMoreRecyclerView.setAdapter(tmAdapter);

    }
    private void loadTattooReviewsData() {

        int postId = getIntent().getIntExtra("postId", -1);

        if(postId != -1) {
            Call<List<TattooReviewItem>> call = networkAPIs.getAllReviews(postId);
            call.enqueue(new Callback<List<TattooReviewItem>>() {
                @Override
                public void onResponse(Call<List<TattooReviewItem>> call, Response<List<TattooReviewItem>> response) {
                    appReviews = (ArrayList<TattooReviewItem>) response.body();
                    tmAdapter.setAppReviewsList(appReviews);
                    tattooReviewMoreRecyclerView.getAdapter().notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<List<TattooReviewItem>> call, Throwable t) {
                    Log.e("DetailNetwork", t.getMessage());
                }
            });
        }

    }
}