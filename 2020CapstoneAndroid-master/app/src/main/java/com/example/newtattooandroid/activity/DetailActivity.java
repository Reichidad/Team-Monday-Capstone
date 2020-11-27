package com.example.newtattooandroid.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtattooandroid.R;
import com.example.newtattooandroid.adapter.SliderAdapter;
import com.example.newtattooandroid.adapter.TattooReviewAdapter;
import com.example.newtattooandroid.model.SliderItem;
import com.example.newtattooandroid.model.TattooistDto;
import com.example.newtattooandroid.model.TattooReviewItem;
import com.example.newtattooandroid.network.NetworkAPIs;
import com.example.newtattooandroid.network.NetworkClient;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DetailActivity extends AppCompatActivity {
    private SliderView sliderView;
    private RecyclerView tattooReviewsRecyclerView;
    private TattooReviewAdapter tAdapter;
    private ArrayList<TattooReviewItem> appReviews;
    //통신
    private Retrofit retrofit;
    private NetworkAPIs networkAPIs;

    //상세 리뷰
    private TextView tv_title;
    private TextView tv_description;
    private TextView tv_clean_rating;
    private RatingBar rb_app;
    private TextView tv_tattoo_users_label;
    private TextView more;
    private TextView tv_price;

    //타투이스트
    private TattooistDto tattooistDto;
    private TextView tv_phone;
    private TextView tv_tattooist_name;
    private TextView tv_tattooist_address;
    private RelativeLayout ly_detail_tattooist;

    //인텐트
    String designUrl = null;
    //Todo : 닉네임 필요함

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //retrofit
        retrofit = NetworkClient.getRetrofitClient(this);
        networkAPIs = retrofit.create(NetworkAPIs.class);

//        //툴바 레이아웃
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
//        collapsingToolbarLayout.setTitle("");

        //도안 실행
        Button simulBtn = findViewById(R.id.btn_simulation);
        designUrl = getIntent().getStringExtra("designUrl");
//        if (getIntent().hasExtra("designUrl")){ //타투 도안 없으면 실행 버튼 사라짐
//            simulBtn.setVisibility(View.GONE);
//        }else{
//            designUrl = getIntent().getStringExtra("designUrl");
//        }
        if (designUrl.equals(null) || designUrl.equals("null")){ //타투 도안 없으면 실행 버튼 사라짐
            simulBtn.setVisibility(View.GONE);
        }else{
            simulBtn.setVisibility(View.VISIBLE);
            designUrl = getIntent().getStringExtra("designUrl");
        }
        simulBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SimulationActivity.class);
                intent.putExtra("designUrl", designUrl);
                getApplicationContext().startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        //뷰페이저, 슬라이드 이미지
        ArrayList<String> urls = (ArrayList<String>) getIntent().getSerializableExtra("urls");

        sliderView = findViewById(R.id.imageSlider);
        SliderAdapter sliderAdapter = new SliderAdapter(this);
        for (String url : urls) {
            sliderAdapter.addItem(new SliderItem(url));
        }
        sliderView.setSliderAdapter(sliderAdapter);

        //상세페이지 리뷰 descriptioon
        tv_tattoo_users_label = findViewById(R.id.tv_tattoo_users_label);
        tv_description = findViewById(R.id.tv_description);
        String descript = getIntent().getStringExtra("description");
        tv_description.setText(descript);

        tv_title = findViewById(R.id.tv_title);
        tv_title.setText(getIntent().getStringExtra("title"));

        tv_price = findViewById(R.id.tv_price);
        tv_price.setText(getIntent().getIntExtra("price", 0) + "만원");

        //글의 clean 점수
        float avgCleanScore = getIntent().getFloatExtra("avgCleanScore", 0);
        float roundScore = (float)(Math.round(avgCleanScore * 10) / 10.0);

        tv_clean_rating = findViewById(R.id.tv_clean_rating);
        tv_clean_rating.setText( String.valueOf(roundScore) );

        rb_app = findViewById(R.id.rb_app);
        rb_app.setRating(roundScore);

        //타투이스트 정보 update
        String tattooistId = getIntent().getStringExtra("tattooistId");

        tv_phone = findViewById(R.id.tv_tattooist_phone);
        tv_tattooist_name = findViewById(R.id.tv_tattooist_name);
        tv_tattooist_address = findViewById(R.id.tv_tattooist_address);

        Call<TattooistDto> call = networkAPIs.getTattoist(tattooistId);
        call.enqueue(new Callback<TattooistDto>() {
            @Override
            public void onResponse(Call<TattooistDto> call, Response<TattooistDto> response) {
                tattooistDto = (TattooistDto) response.body();
                tv_phone.setText(tattooistDto.getMobile()); // 전화번호
                tv_tattooist_name.setText(tattooistDto.getNickName()); // 닉네임
                tv_tattooist_address.setText("(" + tattooistDto.getBigAddress() + " " + tattooistDto.getSmallAddress() + ")");
                Log.e("TattooistSuccess", response.message());
            }

            @Override
            public void onFailure(Call<TattooistDto> call, Throwable t) {
                Log.e("TattooistError", t.getMessage());
            }
        });

        ly_detail_tattooist = findViewById(R.id.ly_detail_tattooist);
        ly_detail_tattooist.setOnClickListener((View view) -> {
            Intent tattooistPostIntent = new Intent(getApplicationContext(), TattooistPostsActivity.class);

            tattooistPostIntent.putExtra("tattooistId", tattooistDto.getUserId());
            tattooistPostIntent.putExtra("tattooistNickName", tattooistDto.getNickName());
            tattooistPostIntent.putExtra("isTattooist", false);
            getApplicationContext().startActivity(tattooistPostIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        });


        //리뷰 데이터 리사이클러 뷰
        tattooReviewsRecyclerView = findViewById(R.id.rv_tattoo_reviews);
        RecyclerView.LayoutManager verticalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        tattooReviewsRecyclerView.setLayoutManager(verticalLayoutManager);
        tattooReviewsRecyclerView.setHasFixedSize(true);
        appReviews = new ArrayList<>();
        tAdapter = new TattooReviewAdapter(appReviews, getApplicationContext());
        tattooReviewsRecyclerView.setAdapter(tAdapter);

        //리뷰데이터 불러오기
        loadTattooReviewsData();


        //리뷰더보기 클릭
        more = findViewById(R.id.more);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //리뷰 더보기 액티비티 실행, ArrayList<AppReviewItem> 넘겨줌
                Intent intent = new Intent(getApplicationContext(), MoreActivity.class);
                intent.putExtra("appReviews", appReviews);
                getApplicationContext().startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
    }

    private void loadTattooReviewsData() {

        int postId = getIntent().getIntExtra("postId", -1);

        if(postId != -1) {
            Call<List<TattooReviewItem>> call = networkAPIs.getAllReviews(postId);
            call.enqueue(new Callback<List<TattooReviewItem>>() {
                @Override
                public void onResponse(Call<List<TattooReviewItem>> call, Response<List<TattooReviewItem>> response) {
                    appReviews = (ArrayList<TattooReviewItem>) response.body();
                    tv_tattoo_users_label.setText(String.valueOf(appReviews.size()));
                    tAdapter.setAppReviewsList(appReviews);
                    tattooReviewsRecyclerView.getAdapter().notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<List<TattooReviewItem>> call, Throwable t) {
                    Log.e("DetailNetwork", t.getMessage());
                }
            });
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: { //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

}
