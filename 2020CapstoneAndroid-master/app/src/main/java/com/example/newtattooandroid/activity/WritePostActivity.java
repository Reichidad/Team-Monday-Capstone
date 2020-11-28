package com.example.newtattooandroid.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.newtattooandroid.R;
import com.example.newtattooandroid.model.MainItem;
import com.example.newtattooandroid.model.TattooistDto;
import com.example.newtattooandroid.network.NetworkAPIs;
import com.example.newtattooandroid.network.NetworkClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class WritePostActivity extends AppCompatActivity {
    // only tattooistID
    private TattooistDto tattooistDto;

    // 통신
    private Retrofit retrofit;
    private NetworkAPIs networkAPIs;

    // ui components
    private ImageButton btn_write_back;
    private EditText et_write_title;
    private EditText et_write_description;
    private EditText et_write_price;
    private EditText et_write_genre;
    private EditText et_write_big_shape;
    private EditText et_write_small_shape;

    private TextView tv_write_images;
    private ImageButton btn_write_image;

    private TextView tv_write_design_name;
    private ImageButton btn_write_design;

    private ImageButton btn_write_post;

    private MultipartBody.Part design;
    private ArrayList<MultipartBody.Part> tattoos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        getTattooist();
        //retrofit
        retrofit = NetworkClient.getRetrofitClient(getApplicationContext());
        networkAPIs = retrofit.create(NetworkAPIs.class);

        btn_write_back = findViewById(R.id.btn_write_back);
        btn_write_back.setOnClickListener((View view) -> {
            finish();
        });

        et_write_title = findViewById(R.id.et_write_title);
        et_write_description = findViewById(R.id.et_write_description);
        et_write_price = findViewById(R.id.et_write_price);
        et_write_genre = findViewById(R.id.et_write_genre);
        et_write_big_shape = findViewById(R.id.et_write_big_shape);
        et_write_small_shape = findViewById(R.id.et_write_small_shape);

        tv_write_images = findViewById(R.id.tv_write_images);
        tv_write_images.setVisibility(View.GONE);
        btn_write_image = findViewById(R.id.btn_write_image);
        tattoos = new ArrayList<>();
        design = null;
        btn_write_image.setOnClickListener((View view) -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, 1);
            tv_write_images.setVisibility(View.VISIBLE);
        });

        tv_write_design_name = findViewById(R.id.tv_write_design_name);
        tv_write_design_name.setVisibility(View.GONE);
        btn_write_design = findViewById(R.id.btn_write_design);
        btn_write_design.setOnClickListener((View view) -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, 2);
        });

        btn_write_post = findViewById(R.id.btn_write_post);
        btn_write_post.setOnClickListener((View view) -> {
            writePost();
            finish();
        });
    }

    private void getTattooist(){
        this.tattooistDto = new TattooistDto();
        tattooistDto.setUserId(getIntent().getStringExtra("tattooistId"));
    }

    private String uriToFileName(Uri uri) {
        Cursor returnCursor =
                getContentResolver().query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }


    private String getPostFromUI(){
        String s = "{";
        s += "\"tattooistId\":" + "\"" + tattooistDto.getUserId() + "\"," +
                "\"title\":" + "\"" + et_write_title.getText().toString() + "\"," +
                "\"description\":" + "\"" + et_write_description.getText().toString() + "\"," +
                "\"price\":" + "\"" + et_write_price.getText().toString() + "\"," +
                "\"genre\":" + "\"" + et_write_genre.getText().toString() + "\"," +
                "\"bigShape\":" + "\"" + et_write_big_shape.getText().toString() + "\"," +
                "\"smallShape\":" + "\"" + et_write_small_shape.getText().toString() + "\"";
        return s + "}";
    }

    private void writePost(){
        Log.e("content : ", getPostFromUI());
        RequestBody post = RequestBody.create(okhttp3.MultipartBody.FORM, getPostFromUI());

        Call<MainItem> call = networkAPIs.writePost(post, tattoos, design);
        call.enqueue(new Callback<MainItem>() {
            @Override
            public void onResponse(Call<MainItem> call, Response<MainItem> response) {

            }

            @Override
            public void onFailure(Call call, Throwable t) { // 성공
                Log.e("image search error", t.toString());
            }
        });
    }

    @SneakyThrows
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String fileName = uriToFileName(data.getData());
        Log.e("select", fileName);
        InputStream in = null;
        try {
            in = getContentResolver().openInputStream(data.getData());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap originalBitmap = BitmapFactory.decodeStream(in);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 256, 256, true);

        File file = new File(getApplicationContext().getCacheDir(), fileName);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
        byte[] bitmapData = bos.toByteArray();

        FileOutputStream fos = null;
        fos = new FileOutputStream(file);
        fos.write(bitmapData);
        fos.flush();
        fos.close();
        Log.e("shrink image", file.getName());

        if (requestCode == 1) { // tattoo image
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/fomr-data"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("tattoos", fileName, requestFile);
            tv_write_images.append(fileName + "\n");

            this.tattoos.add(body);
        }else if(requestCode == 2){ // design
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/fomr-data"), file);
            this.design = MultipartBody.Part.createFormData("design", fileName, requestFile);
            tv_write_design_name.setVisibility(View.VISIBLE);
            tv_write_design_name.setText(fileName);
            btn_write_design.setVisibility(View.GONE);
        }
    }
}
