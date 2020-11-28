package com.example.newtattooandroid.ui.seemore;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.newtattooandroid.R;
import com.example.newtattooandroid.activity.ApplyTattooistActivity;
import com.example.newtattooandroid.activity.TattooistPostsActivity;
import com.example.newtattooandroid.model.TattooistDto;
import com.example.newtattooandroid.model.UserDto;
import com.example.newtattooandroid.network.NetworkAPIs;
import com.example.newtattooandroid.network.NetworkClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SeemoreFragment extends Fragment {
    private SeemoreViewModel seemoreViewModel;

    // data type
    private UserDto userDto;
    private TattooistDto tattooistDto;

    // 통신
    private Retrofit retrofit;
    private NetworkAPIs networkAPIs;

    // intent
    private Intent applyUserIntent;
    private Intent tattooistPostIntent;

    // ui component
    private TextView tv_seemore_user_name;
    private TextView tv_seemore_user_email;
    private Button btn_seemore_modify_userinfo;
    private Button btn_seemore_logout;

    private TextView tv_seemore_my_reviews;

    private TextView tv_seemore_apply_tattooist;
    private TextView tv_seemore_tattooist_posts;

    private LinearLayout ly_seemore_apply_tattooist;
    private LinearLayout ly_seemore_tattooist_posts;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        seemoreViewModel =
                ViewModelProviders.of(this).get(SeemoreViewModel.class);
        View root = inflater.inflate(R.layout.fragment_seemore, container, false);

        //retrofit
        retrofit = NetworkClient.getRetrofitClient(getContext());
        networkAPIs = retrofit.create(NetworkAPIs.class);
        getUser();
        getTattooist();

        // user menu
        tv_seemore_user_name = root.findViewById(R.id.tv_seemore_user_name);
        tv_seemore_user_name.setText(userDto.getName() + " (" + userDto.getNickName() + ")");

        tv_seemore_user_email = root.findViewById(R.id.tv_seemore_user_email);
        tv_seemore_user_email.setText(userDto.getUserId());

        btn_seemore_modify_userinfo = root.findViewById(R.id.btn_seemore_modify_userinfo);
        btn_seemore_logout = root.findViewById(R.id.btn_seemore_logout);

        tv_seemore_my_reviews = root.findViewById(R.id.tv_seemore_my_reviews);
        tv_seemore_my_reviews.setOnClickListener((View view) -> { // 내가 작성한 리뷰 / 리뷰 더보기 참조


        });

        // tattooist menu
        ly_seemore_apply_tattooist = root.findViewById(R.id.ly_seemore_apply_tattooist);
        ly_seemore_apply_tattooist.setOnClickListener((View view)-> {
            Log.e("apply_tattooist", userDto.toString());
            applyUserIntent = new Intent(getActivity().getApplicationContext(), ApplyTattooistActivity.class);

            applyUserIntent.putExtra("userId", userDto.getUserId());
            applyUserIntent.putExtra("name", userDto.getName());
            applyUserIntent.putExtra("nickName", userDto.getNickName());
            getActivity().getApplicationContext().startActivity(applyUserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        });
        tv_seemore_apply_tattooist = root.findViewById(R.id.tv_seemore_apply_tattooist);
//        tv_seemore_apply_tattooist.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) { // 타투이스트 등록
//                Log.e("apply_tattooist", userDto.toString());
//                applyUserIntent = new Intent(getActivity().getApplicationContext(), ApplyTattooistActivity.class);
//
//                applyUserIntent.putExtra("userId", userDto.getUserId());
//                applyUserIntent.putExtra("name", userDto.getName());
//                applyUserIntent.putExtra("nickName", userDto.getNickName());
//                getActivity().getApplicationContext().startActivity(applyUserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//            }
//        });

        ly_seemore_tattooist_posts = root.findViewById(R.id.ly_seemore_tattooist_posts);
        ly_seemore_tattooist_posts.setOnClickListener((View view) -> {
            tattooistPostIntent = new Intent(getActivity().getApplicationContext(), TattooistPostsActivity.class);
            tattooistPostIntent.putExtra("tattooistId", tattooistDto.getUserId());
            tattooistPostIntent.putExtra("tattooistNickName", tattooistDto.getNickName());
            tattooistPostIntent.putExtra("isTattooist", true);
            getActivity().getApplicationContext().startActivity(tattooistPostIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        });
        tv_seemore_tattooist_posts = root.findViewById(R.id.tv_seemore_tattooist_posts);
//        tv_seemore_tattooist_posts.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) { // 게시물 액티비티
//                tattooistPostIntent = new Intent(getActivity().getApplicationContext(), TattooistPostsActivity.class);
//                tattooistPostIntent.putExtra("tattooistId", tattooistDto.getUserId());
//                tattooistPostIntent.putExtra("tattooistNickName", tattooistDto.getNickName());
//                tattooistPostIntent.putExtra("isTattooist", true);
//                getActivity().getApplicationContext().startActivity(tattooistPostIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//            }
//        });

        return root;
    }

    private void getUser(){
        this.userDto = new UserDto();
        userDto.setUserId("donghae0414@naver.com");
        userDto.setUserPw("1");
        userDto.setName("양동욱");
        userDto.setNickName("kyle");
        userDto.setSex(0);
        userDto.setAge(26);
        userDto.setCountry("한국");
        userDto.setAddress("서울");
    }

    private void getTattooist(){
        Call<TattooistDto> call = networkAPIs.getTattoist(userDto.getUserId());
        call.enqueue(new Callback<TattooistDto>() {
            @Override
            public void onResponse(Call<TattooistDto> call, Response<TattooistDto> response) { // exist tattooist
                tattooistDto = (TattooistDto) response.body();
                Log.e("SuccessCall", tattooistDto.toString());

                ly_seemore_apply_tattooist.setVisibility(View.GONE);
                ly_seemore_tattooist_posts.setVisibility(View.VISIBLE);
//                tv_seemore_apply_tattooist.setVisibility(View.GONE);
//                tv_seemore_tattooist_posts.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<TattooistDto> call, Throwable t) { // network error OR No tattooist
                Log.e("NetworkError", t.getMessage());
                tattooistDto = null;

                ly_seemore_apply_tattooist.setVisibility(View.VISIBLE);
                ly_seemore_tattooist_posts.setVisibility(View.GONE);
//                tv_seemore_apply_tattooist.setVisibility(View.VISIBLE);
//                tv_seemore_tattooist_posts.setVisibility(View.GONE);
            }
        });
    }

}