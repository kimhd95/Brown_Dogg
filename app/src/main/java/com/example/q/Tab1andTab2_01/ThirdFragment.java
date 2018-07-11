package com.example.q.Tab1andTab2_01;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ThirdFragment extends Fragment {
    public ThirdFragment(){
    }

    private Intent intent_add, intent_view;
    private ListView listView;
    private ListViewAdapter adapter;
    private ArrayList<ContentModel> contentModelArrayList;
    private SQLiteDatabase DB;
    private ConnectServer connectServer;
    private static final String url = "http://52.231.69.145:8080/";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        connectServer = new ConnectServer();
        View view = inflater.inflate(R.layout.fragment_third,null);
        listView = (ListView) view.findViewById(R.id.lv_LISTVIEW);          // 리스트뷰
        contentModelArrayList = new ArrayList<ContentModel>();  // 저장할 배열




        adapter = new ListViewAdapter(getActivity(), contentModelArrayList);
        listView.setAdapter(adapter);

        /** 2. 아이템 클릭 **/
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String title = contentModelArrayList.get(position).getTitle();
                String author = contentModelArrayList.get(position).getAuthor();
                String text = contentModelArrayList.get(position).getText();

                intent_view = new Intent(getActivity(), viewDetailBoard.class);
                intent_view.putExtra("Title", title);
                intent_view.putExtra("Author", author);
                intent_view.putExtra("Text", text);

                startActivity(intent_view);
            }
        });



        FloatingActionButton add_FAB = (FloatingActionButton) view.findViewById(R.id.add);
        FloatingActionButton refresh_FAB = (FloatingActionButton) view.findViewById(R.id.refresh);
        /** 3. Add **/
        add_FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /** 3-1-1. 새로운 intent 띄움 **/
                intent_add = new Intent(getActivity(), AddItemActivity.class);
                startActivityForResult(intent_add, 1111);
                /** 3-1-2. intent에서 서버에 request **/

                /** 3-3. 새로고침 **/
            }
        });
        /** 1. 새로고침 **/
        refresh_FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("", contentModelArrayList.toString());
                contentModelArrayList.clear();


                OkHttpClient okHttpClient = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder().add("_REFRESH", "1").build();
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
                            JSONObject jsonObject = new JSONObject("{" + "Board" + ":" + res + "}");
                            JSONArray arr = jsonObject.getJSONArray("Board");
                            for (int i = 0; i < arr.length(); i++) {
                                contentModelArrayList.add(new ContentModel(arr.getJSONObject(i).getString("_Title"), arr.getJSONObject(i).getString("_Author"), arr.getJSONObject(i).getString("_Text")));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                try {
                    Thread.sleep(800);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                listView.setAdapter(adapter);
                Toast.makeText(getActivity(), "Refresh", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1111) {
            if(resultCode == 1234) {
                /** 3-3. 새로고침 (추가가 제대로 됐으면) **/
                contentModelArrayList.clear();
                Toast.makeText(getActivity(), "Refresh", Toast.LENGTH_SHORT).show();
                OkHttpClient okHttpClient = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder().add("_REFRESH", "1").build();
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
                            JSONObject jsonObject = new JSONObject("{" + "Board" + ":" + res + "}");
                            JSONArray arr = jsonObject.getJSONArray("Board");
                            for (int i = 0; i < arr.length(); i++) {
                                contentModelArrayList.add(new ContentModel(arr.getJSONObject(i).getString("_Title"), arr.getJSONObject(i).getString("_Author"), arr.getJSONObject(i).getString("_Text")));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                try {
                    Thread.sleep(800);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                listView.setAdapter(adapter);
            }
        }
    }
}
