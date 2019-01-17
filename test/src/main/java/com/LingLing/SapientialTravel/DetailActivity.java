package com.LingLing.SapientialTravel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

public class DetailActivity extends Activity {

    private RatingBar mRatingBar;
    private TextView detailPoiName;
    private TextView detailScenic;
    private TextView price;
    private Button btnPlay;
    private Button btnStop;
    private Button back;
    private String poiName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        initView();
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        poiName = extras.getString("poiName");
        detailPoiName.setText(poiName);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent service = new Intent(DetailActivity.this, MusicServer.class);
                stopService(service);
            }
        });
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent service = new Intent(DetailActivity.this, MusicServer.class);
                startService(service);
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent service = new Intent(DetailActivity.this, MusicServer.class);
                stopService(service);
                finish();
            }
        });
    }

    private void initView() {
        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);
        detailPoiName = (TextView) findViewById(R.id.tv_name_detail_activity);
        detailScenic = (TextView) findViewById(R.id.tv_scenic_overview_activity_detail);
        price = (TextView) findViewById(R.id.price);
        btnPlay = (Button) findViewById(R.id.bt_playMusic);
        btnStop = (Button) findViewById(R.id.bt_stopMusic);
        back = (Button) findViewById(R.id.bt_back_detail_activity);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MusicServer.class);
        startService(intent);
    }
}
