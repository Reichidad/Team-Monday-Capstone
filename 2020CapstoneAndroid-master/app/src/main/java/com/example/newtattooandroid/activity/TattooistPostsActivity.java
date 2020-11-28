package com.example.newtattooandroid.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.newtattooandroid.R;
import com.example.newtattooandroid.adapter.PostAdapter;
import com.example.newtattooandroid.adapter.TattooistPostRecyclerViewDecoration;
import com.example.newtattooandroid.model.MainItem;
import com.example.newtattooandroid.model.TattooistDto;
import com.example.newtattooandroid.network.NetworkAPIs;
import com.example.newtattooandroid.network.NetworkClient;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class TattooistPostsActivity extends AppCompatActivity {
    // recyclerview
    private RecyclerView postsRecyclerView;
    private RecyclerView.LayoutManager postsLayoutManager;
    private PostAdapter postAdapter;
    private ArrayList<MainItem> posts;

    // ui components
    private ImageButton btn_tattooist_posts_back;
    private ImageButton btn_tattooist_write;
    private TextView tv_tattooist_post_name;

    // only id, nickName
    private TattooistDto tattooistDto;
    private boolean isTattooist;

    // 통신
    private Retrofit retrofit;
    private NetworkAPIs networkAPIs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tattooist_posts);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //retrofit
        retrofit = NetworkClient.getRetrofitClient(getApplicationContext());
        networkAPIs = retrofit.create(NetworkAPIs.class);

        btn_tattooist_posts_back = findViewById(R.id.btn_tattooist_post_back);
        btn_tattooist_posts_back.setOnClickListener((View view) -> {
                finish();
        });

        btn_tattooist_write = findViewById(R.id.btn_tattooist_write);
        btn_tattooist_write.setOnClickListener((View view) -> { // 글 작성

            Intent writePostIntent = new Intent(getApplicationContext(), WritePostActivity.class);
            writePostIntent.putExtra("tattooistId", tattooistDto.getUserId());
            getApplicationContext().startActivity(writePostIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        });

        tv_tattooist_post_name = findViewById(R.id.tv_tattooist_post_name);
        getTattooist();

        posts = new ArrayList<>();
        postsRecyclerView = findViewById(R.id.rv_tattooist_post);
        postsLayoutManager = new GridLayoutManager(getApplicationContext(), 4);
        postsRecyclerView.setLayoutManager(postsLayoutManager);

        postAdapter = new PostAdapter(posts, getApplicationContext());
        postsRecyclerView.setAdapter(postAdapter);
        postsRecyclerView.addItemDecoration(new TattooistPostRecyclerViewDecoration(10));

        getTattooistPosts();
    }

    private void getTattooistPosts(){
        Call<List<MainItem>> call = networkAPIs.getTattooistPost(this.tattooistDto.getUserId());
        call.enqueue(new Callback<List<MainItem>>() {
            @Override
            public void onResponse(Call<List<MainItem>> call, Response<List<MainItem>> response) {
//                Log.e("SuccessCall", response.body().toString());
                posts = (ArrayList<MainItem>) response.body();
                postAdapter.setItems(posts);
                postsRecyclerView.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<MainItem>> call, Throwable t) {
                Log.e("NetworkError", t.getMessage());
            }
        });
    }

    private void getTattooist(){
        this.tattooistDto = new TattooistDto();
        this.tattooistDto.setUserId(getIntent().getStringExtra("tattooistId"));
        this.tattooistDto.setNickName(getIntent().getStringExtra("tattooistNickName"));
        this.isTattooist = getIntent().getBooleanExtra("isTattooist", true);

        tv_tattooist_post_name.setText(tattooistDto.getNickName());
        if (this.isTattooist)
            btn_tattooist_write.setVisibility(View.VISIBLE);
        else
            btn_tattooist_write.setVisibility(View.GONE);
    }
}
