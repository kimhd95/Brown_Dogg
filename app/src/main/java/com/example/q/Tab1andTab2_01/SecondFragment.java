package com.example.q.Tab1andTab2_01;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.mongodb.util.JSON;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SecondFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    public SecondFragment(){
    }

    RecyclerView mRecyclerView;
    private GridLayoutManager gridLayoutManager;
    private static final int URL_LOADER = 0;
    public GalleryPickerAdapter adapter;
    String permissions= new String (Manifest.permission.READ_EXTERNAL_STORAGE);
    private int PERMISSION_REQUEST_CODE = 200;
    AlbumView albumView = new AlbumView();
    Button myButton;
    private SwipeRefreshLayout swipeRefreshLayout;



    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //ConstraintLayout layout = (ConstraintLayout) inflater.inflate(R.layout.fragment_second,container,false);

        View view = inflater.inflate(R.layout.gallery_grid,null);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        gridLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(),2);  // 이거는 gallery를 보여주는 것과는 상관없음
        mRecyclerView.setLayoutManager(gridLayoutManager);
        myButton = (Button) view.findViewById(R.id.button2);
        myButton.setOnClickListener(this);
        adapter = new GalleryPickerAdapter(getActivity().getApplicationContext());
        mRecyclerView.setAdapter(adapter);
        //Intent intent = new Intent(getActivity(), GalleryActivity.class);
        //startActivity(intent);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(android.R.color.holo_green_dark),getResources().getColor(android.R.color.holo_red_dark)
                ,getResources().getColor(android.R.color.holo_blue_dark),getResources().getColor(android.R.color.holo_orange_dark) );
        return view;
        //return layout;
    }

    @Override
    public void onClick(View v) {
        String url = "http://52.231.71.25:8080/";
        Toast.makeText(getActivity(), "Backup successful!", Toast.LENGTH_SHORT).show();
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder().add("Backup", "1").build();
        Request request = new Request.Builder().url(url).post(requestBody).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("error", "Connect Server Error is " + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                response.body().close();
                try {
                    JSONObject jsonObject = new JSONObject("{"+"Pictures"+":"+res+"}");
                    JSONArray arr = jsonObject.getJSONArray("Pictures");
                    for(int i=0; i< arr.length(); i++){
                        String encodedImage = arr.getJSONObject(i).getString("Image").replace("\n", "");
                        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        saveImage(decodedByte, arr.getJSONObject(i).getString("Imagename").replace("\n", ""));

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void saveImage(Bitmap finalBitmap, String image_name) {
        String root = Environment.getExternalStorageDirectory().toString()+"/"+Environment.DIRECTORY_DCIM+"/";
        File myDir = new File(root);
        myDir.mkdirs();
        String fname = "Image-" + image_name+ ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        Log.i("LOAD", root + fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(checkPermission()) {
            albumView.loadAlbum();
        }
    }

    private  boolean checkPermission() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        result = ContextCompat.checkSelfPermission(getActivity(),permissions);
        if (result != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(permissions);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(getActivity(), listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {

        if(permsRequestCode==200){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                albumView.loadAlbum();
            } else {
                Toast.makeText(getActivity(),  "Please give permission to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRefresh() {
        FragmentTransaction ft=getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
        swipeRefreshLayout.setRefreshing(false);
    }

    public class AlbumView implements LoaderManager.LoaderCallbacks<Cursor>{

        public void loadAlbum(){
            getActivity().getLoaderManager().restartLoader(URL_LOADER, null, this);//restart the loader manager by invoking getSupportLoaderManager

        }

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return new CursorLoader(getActivity().getApplicationContext(),
                    GalleryPickerAdapter.uri,
                    GalleryPickerAdapter.projections,
                    null,
                    null,
                    GalleryPickerAdapter.sortOrder);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            adapter.setData(PhotosData.getData(true, cursor));
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }



}
