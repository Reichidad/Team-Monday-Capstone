package com.example.newtattooandroid.ui.home;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.newtattooandroid.MainActivity;
import com.example.newtattooandroid.R;
import com.example.newtattooandroid.adapter.ItemAdapter;
import com.example.newtattooandroid.model.MainItem;
import com.example.newtattooandroid.network.NetworkAPIs;
import com.example.newtattooandroid.network.NetworkClient;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HomeFragment extends Fragment implements MaterialSearchBar.OnSearchActionListener{
    private RecyclerView mRecyclerView;
    private ItemAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RequestManager requestManager;
    private List<String> lastSearches;
    private MaterialSearchBar searchBar;
    private ArrayList<MainItem> items;

    //갤러리 관련 변수
    private View mLayout;
    private ImageButton camera_btn;
    private final int GET_CAMERA = 201;
    private final int GET_GALLERY_IMAGE = 200;

    //통신
    private Retrofit retrofit;
    private NetworkAPIs networkAPIs;

    private HomeViewModel homeViewModel;

    private MainActivity mainActivity;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        //retrofit
        retrofit = NetworkClient.getRetrofitClient(getContext());
        networkAPIs = retrofit.create(NetworkAPIs.class);

        //갤러리
        mLayout = getActivity().findViewById(R.id.activity_main);
        camera_btn = root.findViewById(R.id.camera_button);
        camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, GET_GALLERY_IMAGE);
            }
        });

        //searchbar
        searchBar = (MaterialSearchBar) root.findViewById(R.id.searchBar);
        searchBar.setOnSearchActionListener(this);

        mRecyclerView = (RecyclerView) root.findViewById(R.id.rv_main_tattoo);
        mRecyclerView.setHasFixedSize(true);

        mainActivity = (MainActivity) getActivity();
        //RecyclerView설정
        items = new ArrayList<>();
        // 이 부분에서 정렬 방식을 설정합니다.
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        requestManager = Glide.with(this);
        mAdapter = new ItemAdapter(items, getContext(), requestManager, mainActivity);
        mRecyclerView.setAdapter(mAdapter);

        //모든 게시물 조회
        getAllpost();

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_GALLERY_IMAGE) {
//            uploadToServer(data.getData());
            InputStream in = null;
            try{
                in = getActivity().getContentResolver().openInputStream(data.getData());
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            try{
                imageSearch(bitmap);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //save last queries to disk

    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
        String s = enabled ? "enabled" : "disabled";
//        Toast.makeText(MainActivity.this, "Search " + s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSearchConfirmed(CharSequence text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
        searchBar.clearFocus();

        ArrayList<MainItem> list = new ArrayList<>();
        for (MainItem item : items) {
            if (item.getBigShape().contains(text.toString().toLowerCase())) {
                list.add(item);
            }
        }

        mAdapter.setItems(list);
//        mRecyclerView.invalidate();
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }


    @Override
    public void onButtonClicked(int buttonCode) {
        switch (buttonCode) {
            case MaterialSearchBar.BUTTON_BACK:
                mAdapter.setItems(items);
//                mRecyclerView.invalidate();
                mRecyclerView.getAdapter().notifyDataSetChanged();
                Log.e("MyLog", items.toString());
                break;
        }
    }

    //게시물 전체 정보 요청
    private void getAllpost(){
        Log.d("main", "get all post request");
        Call<List<MainItem>> call = networkAPIs.getAllPost();
        call.enqueue(new Callback<List<MainItem>>() {
            @Override
            public void onResponse(Call<List<MainItem>> call, Response<List<MainItem>> response) {
//                Log.e("SuccessCall", response.body().toString());
                items = (ArrayList<MainItem>) response.body();

                // TODO 정렬
                Collections.sort(items, (MainItem m1, MainItem m2) -> {
                    if (m1.getAvgCleanScore() < m2.getAvgCleanScore()){
                        return 1;
                    } else {
                        return -1;
                    }
                });

                mAdapter.setItems(items);
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<MainItem>> call, Throwable t) {
                Log.e("NetworkError", t.getMessage());
            }
        });
    }

    // 이미지 서치
    private void imageSearch(Bitmap originalBitmap) throws IOException {
//        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 256, 256, true);
        File file = new File(getActivity().getApplicationContext().getCacheDir(), "temp");
        file.createNewFile();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        originalBitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
        byte[] bitmapData = bos.toByteArray();

        FileOutputStream fos = null;
        fos = new FileOutputStream(file);
        fos.write(bitmapData);
        fos.flush();
        fos.close();

        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("image", file.getName(), fileReqBody);

        Call<List<MainItem>> call = networkAPIs.searchImage(part);
        call.enqueue(new Callback<List<MainItem>>() {
            @Override
            public void onResponse(Call<List<MainItem>> call, Response<List<MainItem>> response) {
                ArrayList<MainItem> searchResults = (ArrayList<MainItem>) response.body();
//                for (MainItem m : searchResults) {
//                    Log.d("image search", m.toString());
//                }
                mAdapter.setItems(searchResults);
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }
            @Override
            public void onFailure(Call call, Throwable t) {
                Log.e("image search error", t.toString());
            }
        });
    }

    //서버에 이미지 전송
    private void uploadToServer(Uri imgUri) {
        String filePath = getRealPathFromURI(imgUri);

        //Create a file object using file path
        File file = new File(filePath);

        // Create a request body with file and image media type
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
        // Create MultipartBody.Part using file request-body,file name and part name
        MultipartBody.Part part = MultipartBody.Part.createFormData("upload", file.getName(), fileReqBody);
        //Create request body with text description and text media type
        RequestBody description = RequestBody.create(MediaType.parse("text/plain"), "image-type");
        //

        Call call = networkAPIs.uploadImage(part, description);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
            }
            @Override
            public void onFailure(Call call, Throwable t) {
            }
        });
    }

    //이미지 절대주소 가져오기
    private String getRealPathFromURI(Uri contentUri) {

        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(contentUri, proj, null, null, null);
        cursor.moveToFirst();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        return cursor.getString(column_index);
    }

    private void displayUserInfo() {
        String name = getActivity().getIntent().getStringExtra("username");
        if (name != null) {
            Toast.makeText(getContext(), name + "님 반갑습니다", Toast.LENGTH_LONG).show();
        }
    }

}