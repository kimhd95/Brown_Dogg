package com.example.q.Tab1andTab2_01;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by vaishakha on 18/10/16.
 */
public class ImagesGridActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    RecyclerView mRecyclerView;
    private GridLayoutManager gridLayoutManager;
    private static final int URL_LOADER = 0;
    GalleryPickerAdapter adapter;
    static int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_grid);

        Intent intent = getIntent();
        position = intent.getIntExtra("POS",0);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        gridLayoutManager = new GridLayoutManager(getApplicationContext(),2);

        mRecyclerView.setLayoutManager(gridLayoutManager);
        int spacing = 0; // 50px
        boolean includeEdge = false;
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacing, includeEdge));

        getLoaderManager().initLoader(URL_LOADER, null, this);

        if (adapter == null) {
            adapter = new GalleryPickerAdapter(getApplicationContext());
            mRecyclerView.setAdapter(adapter);
        }
    }

    public void onClick(View v) {
        String url = "http://52.231.71.25:8080/";
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
                        byte[] decodedString = android.util.Base64.decode(encodedImage, Base64.DEFAULT);
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
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root);
        myDir.mkdirs();
        String fname = "Image-" + image_name;
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        Log.i("LOAD", root+fname ) ;
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        if (id == URL_LOADER) {
            return new CursorLoader(getApplicationContext(),
                    GalleryPickerAdapter.uri,
                    GalleryPickerAdapter.projections,
                    GalleryPickerAdapter.projections[3] + " = \"" + GalleryPickerAdapter.data.get(position).getBucketId() + "\"",
                    null,
                    GalleryPickerAdapter.sortOrder);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.setData(PhotosData.getData(false, cursor));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        adapter.notifyDataSetChanged();
    }

}