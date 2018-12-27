package com.kk.siriwave;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private SiriWaveView siriWaveView;
    private Button plusBtn;
    private TextView volumeText;
    private Button reduceBtn;

    private int curVolume = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        siriWaveView = findViewById(R.id.siri_wave_view);
        plusBtn = findViewById(R.id.plus_btn);
        volumeText = findViewById(R.id.volume_text);
        reduceBtn = findViewById(R.id.reduce_btn);

        volumeText.setText(String.valueOf(curVolume));

        siriWaveView.startAnim();
        siriWaveView.setVolume(curVolume);

        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curVolume ++;
                volumeText.setText(String.valueOf(curVolume));
                siriWaveView.setVolume(curVolume);
            }
        });

        reduceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curVolume --;
                volumeText.setText(String.valueOf(curVolume));
                siriWaveView.setVolume(curVolume);
            }
        });
    }
}
