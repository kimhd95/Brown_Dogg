package com.example.q.Tab1andTab2_01;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class FirstFragment extends Fragment{
    public FirstFragment(){
    }

    private Intent intent;
    private ListView listView;
    private ImageButton btnScrollTop;

    private  FirstFragmentListViewAdapter firstFragmentListViewAdapter;
    private ArrayList<FirstFragmentContactModel> firstFragmentContactModelArrayList;
    private final static String SpecialNumber = "010-0000-0000";
    SQLiteDatabase db;
    private static final String url = "http://52.231.69.145:8080/";



    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ConnectServer connectServer = new ConnectServer();
        View view = inflater.inflate(R.layout.fragment_first,null);
        listView = (ListView)view.findViewById(R.id.listview1);
        firstFragmentContactModelArrayList = new ArrayList<>();

        /** 통화기록 DB에 저장 **/
        File f = new File(getActivity().getFilesDir(), "callLogANDcontact.db");
        try {
            db = SQLiteDatabase.openOrCreateDatabase(f, null);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        if (db != null) {
            db.execSQL("CREATE TABLE IF NOT EXISTS callHistory (Name TEXT, " +
                    "Phone TEXT, Duration TEXT, Type TEXT, Date TEXT)");

            // db에 통화기록 insert
            String[] projection = {CallLog.Calls.CACHED_NAME, CallLog.Calls.NUMBER, CallLog.Calls.DURATION, CallLog.Calls.TYPE, CallLog.Calls.DATE};
            Cursor callog = getContext().getContentResolver().query(CallLog.Calls.CONTENT_URI,
                    projection, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
            while(callog.moveToNext()) {
                String name = callog.getString(0);
                String phone = callog.getString(1);
                String duration = callog.getString(2);
                String type = callog.getString(3);
                String date = callog.getString(4);
                Cursor cs = db.rawQuery("SELECT name FROM callHistory WHERE Name=\'" +
                        name + "\' and Phone=\'" + phone + "\' and Duration=\'" + duration +
                        "\' and Type=\'" + type + "\' and Date=\'" + date + "\'", null);
                String sql = "INSERT INTO callHistory VALUES (\"" + name + "\", \"" + phone + "\", \"" + duration + "\", \"" + type + "\", \"" + date + "\")";
                if (cs.getCount() < 1) {
                    db.execSQL(sql);
                    JSONObject jsonObj = new JSONObject();
                    try {
                        jsonObj.put("Name", name);
                        jsonObj.put("Phone", phone);
                        jsonObj.put("Duration", duration);
                        jsonObj.put("Type", type);
                        jsonObj.put("_Date", date);
                        connectServer.requestPost(url, jsonObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /** 0. mongoDB와 내부DB 동기화 **/





        // 1. SQLite DB에서 전화부 가져옴
        /** a-1. 새로 추가된 연락처 내부 디비에 저장 **/
        final Cursor phones = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");

        while(phones.moveToNext()) {
            String cur_name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String cur_phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("Name", cur_name);
                jsonObj.put("Phone", cur_phone);
                connectServer.requestPost(url, jsonObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            firstFragmentContactModelArrayList.add(new FirstFragmentContactModel(cur_name, cur_phone));

            if (firstFragmentContactModelArrayList.size() > 1) {
                String prev = getInitialSound(firstFragmentContactModelArrayList.get(firstFragmentContactModelArrayList.size() - 2).getName());
                String curr = getInitialSound(firstFragmentContactModelArrayList.get(firstFragmentContactModelArrayList.size() - 1).getName());
                Log.d("Test-1", "prev : " + prev);
                Log.d("Test-2", "curr : " + curr);

                if (prev != null) {
                    if (!prev.equals(curr)) {
                        FirstFragmentContactModel korean_character = new FirstFragmentContactModel();
                        korean_character.setName(firstFragmentContactModelArrayList.get(firstFragmentContactModelArrayList.size() - 1).getName());
                        korean_character.setNumber(SpecialNumber);
                        firstFragmentContactModelArrayList.add(firstFragmentContactModelArrayList.size() - 1, korean_character);
                    }
                }
            }
        }
        phones.close();


        // for korean letter "ㄱ"
        FirstFragmentContactModel korean_letter = new FirstFragmentContactModel();
        korean_letter.setName("기역");
        korean_letter.setNumber(SpecialNumber);
        firstFragmentContactModelArrayList.add(0, korean_letter);

        firstFragmentListViewAdapter = new FirstFragmentListViewAdapter(getActivity(), firstFragmentContactModelArrayList);
        listView.setAdapter(firstFragmentListViewAdapter);









        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor_callDB = db.rawQuery("SELECT all Name, Phone, Duration, Type FROM callHistory WHERE Name=\"" +
                        firstFragmentContactModelArrayList.get(position).getName() + "\"", null);
                if (cursor_callDB.getCount() > 0) {
                    String call_time;               // 총 통화횟수
                    int receive_time = 0;           // 수신 횟수
                    int send_time = 0;              // 발신 횟수
                    int absence_time = 0;           // 부재중 횟수
                    String dur;                     // dur : 통화시간

                    cursor_callDB.moveToFirst();
                    call_time = Integer.toString(cursor_callDB.getCount());

                    String name = cursor_callDB.getString(0);
                    String phone = cursor_callDB.getString(1);

                    int dur_i = 0;
                    do {
                        dur = cursor_callDB.getString(2);
                        dur_i += Integer.parseInt(dur);
                        String type = cursor_callDB.getString(3);
                        if(type.equals("1"))
                            receive_time++;
                        else if(type.equals("2"))
                            send_time++;
                        else if(type.equals("3"))
                            absence_time++;
                    } while (cursor_callDB.moveToNext());
                    dur = Integer.toString(dur_i);
                    String hr = Integer.toString(Integer.parseInt(dur) / 3600);
                    if (hr.length() == 1)
                        hr = "0" + hr;
                    String mn = Integer.toString((Integer.parseInt(dur) / 60) % 60);
                    if (mn.length() == 1)
                        mn = "0" + mn;
                    String sc = Integer.toString(Integer.parseInt(dur) % 60);
                    if (sc.length() == 1)
                        sc = "0" + sc;
                    dur = hr + " : " + mn + " : " + sc;

                    int receive_percent = 100*receive_time / Integer.parseInt(call_time);
                    int send_percent = 100*send_time / Integer.parseInt(call_time);
                    int absence_percent = 100*absence_time / Integer.parseInt(call_time);
                    String rec = Integer.toString(receive_time) + " ( " + Integer.toString(receive_percent) + "%)";
                    String snd = Integer.toString(send_time) + " ( " + Integer.toString(send_percent) + "%)";
                    String abs = Integer.toString(absence_time) + " ( " + Integer.toString(absence_percent) + "%)";

                    intent = new Intent(getActivity(), ContactDetailPage.class);
                    intent.putExtra("name", name);
                    intent.putExtra("phone", phone);
                    intent.putExtra("duration", dur);
                    intent.putExtra("call", call_time);
                    intent.putExtra("receive", rec);
                    intent.putExtra("send", snd);
                    intent.putExtra("absence", abs);
                    startActivity(intent);

                    // Toast.makeText(getActivity(), Integer.toString(send_time) + "(" + Integer.toString(send_percent) + "%)", Toast.LENGTH_LONG).show();
                }  else {
                    String name = firstFragmentContactModelArrayList.get(position).getName();
                    String phone = firstFragmentContactModelArrayList.get(position).getNumber();

                    intent = new Intent(getActivity(), ContactDetailPage.class);
                    intent.putExtra("name", name);
                    intent.putExtra("phone", phone.substring(1));
                    intent.putExtra("duration", "00:00:00");
                    intent.putExtra("call", "0");
                    intent.putExtra("receive", "0");
                    intent.putExtra("send", "0");
                    intent.putExtra("absence", "0");
                    startActivity(intent);
                }
            }
        });

        btnScrollTop = (ImageButton)view.findViewById(R.id.imageScrollTop);
        btnScrollTop.setOnClickListener(new ImageButton.OnClickListener(){
            @Override
            public void onClick(View v) {
                //listView.smoothScrollByOffset(0);
                try {
                    Thread.sleep(1200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getActivity(), "연락처와 통화기록이 서버에 저장되었습니다.", Toast.LENGTH_SHORT).show();
            }});


        return view;
    }

    private String getInitialSound(String text) {
        String[] chs = {
                "ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
        };
        if(text.length() > 0) {
            char chName = text.charAt(0);
            if(chName >= 0xAC00)
            {
                int uniVal = chName - 0xAC00;
                int cho = ((uniVal - (uniVal % 28))/28)/21;

                return chs[cho];
            }
        }
        return null;
    }

}