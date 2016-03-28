package com.lwf.reboundhorizontalscrollview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ReboundHorizontalScrollView reboundHorizontalScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        reboundHorizontalScrollView = (ReboundHorizontalScrollView) findViewById(R.id.reboundHorizontalScrollView);
        reboundHorizontalScrollView.setOnReboundListtener(new ReboundHorizontalScrollView.OnReboundListener() {
            @Override
            public void OnLeftRebound() {
                Toast.makeText(MainActivity.this, "触发左侧事件", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void OnRightRebound() {
                Toast.makeText(MainActivity.this, "触发右侧事件", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
