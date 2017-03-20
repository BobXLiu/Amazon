package com.android.test.amazon.avs;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

public class RecordActivity extends AppCompatActivity {

    private Button btnRecord, btnPlay;
    private MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        init();
    }

    private void init() {
        mediaPlayer =  new MediaPlayer();

        btnRecord = (Button) findViewById(R.id.btn_record);


        btnRecord.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnRecord.setText(getResources().getString(R.string.btn_release_to_stop_record));
                        startRecord();
                        break;
                    case MotionEvent.ACTION_UP:
                        btnRecord.setText(getResources().getString(R.string.btn_press_to_start_record));
                        stopRecord();
                        break;
                }

                return false;
            }
        });


        btnPlay = (Button) findViewById(R.id.btn_paly);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioRecordFunc audioRecordFunc = AudioRecordFunc.getInstance(RecordActivity.this);
                String fileName = audioRecordFunc.getNewAudioName();

                if(mediaPlayer == null){
                    mediaPlayer = new MediaPlayer();
                }

                if(mediaPlayer != null){
                    try {
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(fileName);
                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mediaPlayer) {
                                mediaPlayer.start();
                            }
                        });
//                        mediaPlayer.prepare();
                        mediaPlayer.prepareAsync();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                RecordActivity.this.mediaPlayer.release();
                RecordActivity.this.mediaPlayer = null;
                Log.i("Bob","play finish");
            }
        });
    }

    private void startRecord() {
        int mResult = -1;

        AudioRecordFunc mRecord_1 = AudioRecordFunc.getInstance(RecordActivity.this);
        mResult = mRecord_1.startRecordAndFile();
    }

    /**
     * 停止录音
     */
    private void stopRecord() {
        AudioRecordFunc mRecord_1 = AudioRecordFunc.getInstance(RecordActivity.this);
        mRecord_1.stopRecordAndFile();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null){
            mediaPlayer.release();
        }
    }
}
