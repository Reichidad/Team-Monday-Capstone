package com.example.newtattooandroid.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.newtattooandroid.R;
import com.example.newtattooandroid.model.TattooistDto;
import com.example.newtattooandroid.model.UserDto;
import com.example.newtattooandroid.network.NetworkAPIs;
import com.example.newtattooandroid.network.NetworkClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ApplyTattooistActivity extends AppCompatActivity {

    private UserDto userDto;
    private TattooistDto tattooistDto;

    // 통신
    private Retrofit retrofit;
    private NetworkAPIs networkAPIs;

    private TextView tv_apply_user_name;
    private ImageButton btn_apply_back;
    private ImageButton btn_tattooist_apply;

    private EditText et_apply_big_address;
    private EditText et_apply_small_address;
    private EditText et_apply_mobile;
    private EditText et_apply_description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_tattooist);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //retrofit
        retrofit = NetworkClient.getRetrofitClient(getApplicationContext());
        networkAPIs = retrofit.create(NetworkAPIs.class);

        getUserDto();
        tv_apply_user_name = findViewById(R.id.tv_apply_user_name);
        tv_apply_user_name.setText(userDto.getName() + " (" + userDto.getNickName() + ")");

        et_apply_big_address = findViewById(R.id.et_apply_big_address);
        et_apply_small_address = findViewById(R.id.et_apply_small_address);
        et_apply_mobile = findViewById(R.id.et_apply_mobile);
        et_apply_description = findViewById(R.id.et_apply_description);

        btn_apply_back = findViewById(R.id.btn_apply_back);
        btn_apply_back.setOnClickListener((View view) -> {
            finish();
        });

        btn_tattooist_apply = findViewById(R.id.btn_tattooist_apply);
        btn_tattooist_apply.setOnClickListener((View view) -> { // 타투이스트 신청
            applyTattooist();
        });
    }

    private void getUserDto(){
        this.userDto = new UserDto();
        userDto.setUserId(getIntent().getStringExtra("userId"));
        userDto.setName(getIntent().getStringExtra("name"));
        userDto.setNickName(getIntent().getStringExtra("nickName"));
//        userDto.setUserId("b@naver.com");
//        userDto.setName("양인수");
//        userDto.setNickName("마이화나");
    }

    private void applyTattooist(){
        tattooistDto = new TattooistDto();
        tattooistDto.setUserId(userDto.getUserId());
        tattooistDto.setNickName(userDto.getNickName());
        tattooistDto.setBigAddress(et_apply_big_address.getText().toString());
        tattooistDto.setSmallAddress(et_apply_small_address.getText().toString());
        tattooistDto.setMobile(et_apply_mobile.getText().toString());
        tattooistDto.setDescription(et_apply_description.getText().toString());

        Call<TattooistDto> call = networkAPIs.applyTattooist(tattooistDto);
        call.enqueue(new Callback<TattooistDto>() {
            @Override
            public void onResponse(Call<TattooistDto> call, Response<TattooistDto> response) { // 실행되면 이상한 거
                tattooistDto = (TattooistDto) response.body();
                Log.e("SuccessCall", tattooistDto.toString());
            }

            @Override
            public void onFailure(Call<TattooistDto> call, Throwable t) { // return void라 이게 성공
                Log.e("NetworkError", t.getMessage());
            }
        });
    }
}
