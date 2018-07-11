package com.example.q.Tab1andTab2_01;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ImageViewActivity extends AppCompatActivity {
    TextView totalCount,currentCount;
    ViewPager viewPager;
    ViewPagerAdapter pageradapter;
    int position;
    ArrayList<String> list;
    Context context;
    String imagename;
    private static final String url = "http://52.231.69.145:8080";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_pager);
        context = getApplicationContext();
        totalCount = (TextView) findViewById(R.id.total_images);
        currentCount = (TextView) findViewById(R.id.current_image);
        viewPager = (ViewPager) findViewById(R.id.pager);

        Intent intent = getIntent();
        position = intent.getExtras().getInt("Position");
        list = intent.getStringArrayListExtra("paths");
        imagename = intent.getExtras().getString("photoname");


        pageradapter = new ViewPagerAdapter(context,list);
        viewPager.setAdapter(pageradapter);
        viewPager.setCurrentItem(position);

        currentCount.setText((position+1) +"");
        totalCount.setText(list.size()+"");
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentCount.setText((position+1)+"");
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    public String converToBase64(String imagePath) {
        Bitmap bm = BitmapFactory.decodeFile(imagePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteArrayImage = baos.toByteArray();
        String encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
        Log.d("11", encodedImage);
        return encodedImage;
    }

    public void sendimage(View v) {
        Toast.makeText(this, "서버 업로드에 성공하였습니다.", Toast.LENGTH_SHORT).show();
        /** c. jsonArray 서버에 전송 **/
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder().add("Image", converToBase64(list.get(Integer.parseInt(currentCount.getText().toString())-1)).replace("\n", "")).add("Imagename", imagename).build();
        Request request = new Request.Builder().url(url).post(requestBody).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("error", "Connect Server Error is " + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("aaaa", "Response Body is " + response.body().string());
            }
        });

    }


    private class ViewPagerAdapter extends PagerAdapter {
        Context context;
        LayoutInflater mLayoutInflater;
        ArrayList<String> list;

        public ViewPagerAdapter(Context context, ArrayList<String> list) {
            this.list = list;
            this.context = context;
            mLayoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((LinearLayout) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);

            TouchImageView imageView = (TouchImageView) itemView.findViewById(R.id.pager_image);

            imageView.setImageBitmap(BitmapFactory.decodeFile(list.get(position)));
            container.addView(itemView);

            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }

    }
}