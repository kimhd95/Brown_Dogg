package com.example.q.Tab1andTab2_01;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class viewDetailBoard extends AppCompatActivity {
    private TextView __Title, __Text, __Author;
    private Button button;
    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_detail_board);

        __Title = (TextView)findViewById(R.id.title_tv);
        __Text = (TextView)findViewById(R.id.text_tv);
        __Author = (TextView)findViewById(R.id.author_tv);
        button = (Button)findViewById(R.id.button_board);

        intent = getIntent();
        String _tit = intent.getStringExtra("Title");
        String _ath = intent.getStringExtra("Author");
        String _txt = intent.getStringExtra("Text");

        __Title.setText(_tit);
        __Author.setText(_ath);
        __Text.setText(_txt);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}
