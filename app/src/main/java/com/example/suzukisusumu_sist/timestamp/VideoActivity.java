package com.example.suzukisusumu_sist.timestamp;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.Timer;
import java.util.TimerTask;

public class VideoActivity extends AppCompatActivity {

    public VideoView video;
    public TextView counter,t_Xpoint,t_Ypoint;
    public SeekBar seekBar;
    public ImageButton b_start;
    public String androidId;
    public long lastSubmitTime=0;
    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        video = (VideoView) findViewById(R.id.videoView);
        b_start = (ImageButton) findViewById(R.id.start);
        t_Xpoint = (TextView) findViewById(R.id.pointX);
        t_Ypoint=(TextView) findViewById(R.id.pointY);
        seekBar =(SeekBar) findViewById(R.id.seekBar);
        counter = (TextView) findViewById(R.id.Counter);
        intent = getIntent();
        androidId= android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        //動画メディアの指定
        if(null==intent.getData()) {
            Log.d("intentfalse","finish");
            finish();
        }
        else {
            Log.d("intenttrue","finish");
            buttonClickListener();
            Uri uri = intent.getData();
            Log.d("URL", "finish");
            //黙示的インテントを行うとtimestamp://動画URLが返ってくるため、schemeEditを行いHTTPに変換する。
            VideoChange(schemeEdit(intent.getData(), "http"));

        //再生時間表示に関する処理
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    counter.post(new Runnable() {
                        @Override
                        public void run() {
                            counter.setText(String.valueOf(video.getCurrentPosition() / 1000) + "s");
                            seekBar.setProgress(100 * video.getCurrentPosition() / video.getDuration());
                        }
                    });
                }
            }, 0, 50);


            //次動画自動再生処理
            video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mp.getCurrentPosition() != mp.getDuration()) {
                        finish();
                    }
                    VideoChange(schemeEdit(intent.getData(), "http"));
                }
            });

            //動画をタッチしたときの処理
            video.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent e) {
                    if (System.currentTimeMillis() - lastSubmitTime > 200) {
                        AsyncHttp post;
                        int i;
                        if (video.isPlaying()) i = 1;
                        else i = 0;

                        post = new AsyncHttp(e.getX() / v.getWidth(), e.getY() / v.getHeight(), video.getCurrentPosition(), intent.getData().getPath().substring(1), androidId, i);
                        post.execute();
                        lastSubmitTime = System.currentTimeMillis();
                    }
                    t_Xpoint.setText(String.valueOf(e.getX() / v.getWidth()));
                    t_Ypoint.setText(String.valueOf(e.getY() / v.getHeight()));
                    return true;
                }
            });
            //シークバーの処理
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    video.seekTo(video.getDuration() * seekBar.getProgress() / 100);
                    t_Xpoint.setText(String.valueOf(video.getDuration()));
                }
            });
        }
    }

    //ボタンクリックリスナーの設定をまとめる
    public void buttonClickListener(){

        b_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (video.isPlaying()) {
                    video.pause();
                } else {
                    video.start();
                }
            }
        });
   }

    private int VideoChange(Uri path){
        video.setVideoURI(path);
        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                video.seekTo(0);
                video.start();
            }
        });
        return 0;
    }

    public Uri schemeEdit(Uri uri , String scheme){
        Uri.Builder retUri = new Uri.Builder();
        retUri.scheme(scheme);
        retUri.authority(uri.getAuthority());
        retUri.path(uri.getPath());
        return retUri.build();
    }


}