package com.example.q.Tab1andTab2_01;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

public class AddItemActivity extends AppCompatActivity {
    private Intent intent;
    private EditText title_EditText, text_EditText;
    private static final String url = "http://52.231.69.145:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final ConnectServer connectS = new ConnectServer();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        title_EditText = (EditText)findViewById(R.id.title__EDITTEXT);
        text_EditText = (EditText)findViewById(R.id.text_EDITTEXT);
        Button button = (Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent();

                JSONObject json_send = new JSONObject();
                try {
                    json_send.put("_ADD", 1);
                    json_send.put("_title", title_EditText.getText().toString());
                    json_send.put("_text", text_EditText.getText().toString());
                    json_send.put("_author", "anonymous");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                /** 3-1. 서버에 저장하라고 request **/
                connectS.requestPost(url, json_send);
                
                setResult(1234, intent);
                finish();
            }
        });


    }
}
