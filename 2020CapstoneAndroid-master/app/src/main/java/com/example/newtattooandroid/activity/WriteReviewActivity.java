package com.example.newtattooandroid.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.newtattooandroid.R;
import lombok.SneakyThrows;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import java.io.*;

public class WriteReviewActivity extends AppCompatActivity {

    private ImageButton btn_write_back;

    //타투 이미지 첨부 관련
    private TextView tv_write_images;
    private ImageButton btn_write_image;
    //시술 장소 이미지 첨부 관련
    private TextView tv_write_place;
    private ImageButton btn_write_place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        //기존 액션바 숨기기
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //작성 취소 버튼 - 클릭시 종료
        btn_write_back = findViewById(R.id.btn_write_back);
        btn_write_back.setOnClickListener((View view) -> {
            finish();
        });

        //타투 리뷰 이미지 버튼 - 사진 첨부
        tv_write_images = findViewById(R.id.tv_write_images);
        tv_write_images.setVisibility(View.GONE);
        btn_write_image = findViewById(R.id.btn_write_image);
        btn_write_image.setOnClickListener((View view) -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, 1);
            tv_write_images.setVisibility(View.VISIBLE);
        });

        //타투 시술 장소 이미지 버튼 - 사진 첨부
        tv_write_place = findViewById(R.id.tv_write_place);
        tv_write_place.setVisibility(View.GONE);
        btn_write_place = findViewById(R.id.btn_write_place);
        btn_write_place.setOnClickListener((View view) -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, 2);
        });
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
            tv_write_images.append(fileName + "\n");
        }else if(requestCode == 2){ // design
            tv_write_place.setVisibility(View.VISIBLE);
            tv_write_place.setText(fileName);
            btn_write_place.setVisibility(View.GONE);
        }
    }
}
