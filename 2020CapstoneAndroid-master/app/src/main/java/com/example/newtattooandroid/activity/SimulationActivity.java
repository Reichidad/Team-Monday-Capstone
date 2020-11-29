package com.example.newtattooandroid.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapThumbnailImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.newtattooandroid.R;
import com.example.newtattooandroid.gesture.SandboxView;
import com.example.newtattooandroid.network.NetworkAPIs;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.*;


public class SimulationActivity extends AppCompatActivity {

    private SandboxView sandboxView;
    private LinearLayout loadLayout;
    private ImageView bgimageView;
    private LinearLayout saveLayout;
    private FrameLayout captureLayout;
    private Button button;

    //갤러리에서 이미지 가져오기 코드
    private final int GET_GALLERY_IMAGE = 200;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulation);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        sandboxView = (SandboxView) findViewById(R.id.view_sandbox);
        loadLayout = (LinearLayout) findViewById(R.id.ll_load);
        bgimageView = findViewById(R.id.iv_bg);
        saveLayout = findViewById(R.id.ll_save);
        captureLayout = findViewById(R.id.fl_capture);
        button = (Button) findViewById(R.id.button);

//        도안 이미지 불러오기
        String designUrl = getIntent().getStringExtra("designUrl");
//        Log.e("url",designUrl);
        if(designUrl.equals(null) || designUrl.equals("null")){
            //아무것도 안함
        }else{
            Glide.with(getApplicationContext()).asBitmap().load(designUrl).override(600, 600).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    sandboxView.setImageBitmap(resource);
                }
            });
        }


        //저장하기 Layout, 불러오기 Layout 클릭 이벤트 처리
        loadLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, GET_GALLERY_IMAGE);
            }
        });


        saveLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/tattoo";
                String fileName = String.format("%s%d.jpg", "result",System.currentTimeMillis());
                File file = new File(path);
                if(!file.exists()){
                    file.mkdir();
                    Toast.makeText(SimulationActivity.this, "폴더가 생성되었습니다.", Toast.LENGTH_SHORT).show();
                }
                File outFile = new File(file, fileName);
                captureLayout.buildDrawingCache();
                Bitmap captureview = captureLayout.getDrawingCache();

                FileOutputStream fos = null;
                try{
                    fos = new FileOutputStream(outFile);
                    captureview.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(outFile)));
                    Toast.makeText(SimulationActivity.this, "저장이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    fos.flush();
                    fos.close();
                    captureLayout.destroyDrawingCache();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    BitmapDrawable drawable = (BitmapDrawable) bgimageView.getDrawable();
                    Bitmap bgBitmap = drawable.getBitmap();
                    getImageDepth(bgBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GET_GALLERY_IMAGE){
            if(resultCode == RESULT_OK){
                try{
                    InputStream in = getContentResolver().openInputStream(data.getData());

                    Bitmap img = BitmapFactory.decodeStream(in);
                    //회전정보 가져오기
                    ExifInterface exif = null;
                    exif = new ExifInterface(in);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                    Bitmap bmRotated = roteateBitmap(img, orientation);

                    in.close();

                    Glide.with(getApplicationContext())
                            .load(bmRotated)
                            .fitCenter()
                            .into(bgimageView);
//                    bgimageView.setImageBitmap(img);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(resultCode == RESULT_CANCELED){
                Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
            }

        }
    }

    //갤러리 이미지 회전 현상 해결 목적 (비트맵 회전)
     private Bitmap roteateBitmap(Bitmap bitmap, int orientation){
        Matrix matrix = new Matrix();
        switch (orientation){
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try{
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();;
            return bmRotated;
        }catch(OutOfMemoryError e){
            e.printStackTrace();
            return null;
        }
    }

    //이미지뎁스 구하기
    private void getImageDepth(Bitmap tattooImage) throws IOException {
//        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 256, 256, true);
        File file = new File(getApplicationContext().getCacheDir(), "temp");
        file.createNewFile();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        tattooImage.compress(Bitmap.CompressFormat.PNG, 0, bos);
        byte[] bitmapData = bos.toByteArray();

        FileOutputStream fos = null;
        fos = new FileOutputStream(file);
        fos.write(bitmapData);
        fos.flush();
        fos.close();

        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("image", file.getName(), fileReqBody);

        NetworkAPIs api = new Retrofit.Builder().baseUrl("http://motion2ai.iptime.org:5000/").build().create(NetworkAPIs.class);
        Call<ResponseBody> call = api.getImageDepth(part);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                InputStream is = response.body().byteStream();
                Bitmap bm = BitmapFactory.decodeStream(is);
                sandboxView.setDepthImageBitmap(bm);
                sandboxView.setBackgroundValues(bgimageView.getWidth(), bgimageView.getHeight());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
}