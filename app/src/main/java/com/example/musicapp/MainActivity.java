//곡 검색 기능

package com.example.musicapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.View;
import android.widget.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView songListView;
    Button btnPlay, btnPause, btnStop;
    TextView songName, currentTime, fullTime;
    String selected_song;
    SeekBar progress;
    CheckBox rePlay;

    ArrayList<String> songList;
    MediaPlayer song_Player;
    String song_Path = Environment.getExternalStorageDirectory().getPath() + "/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("채운's Music Player");

        ActivityCompat.requestPermissions(this, new String[]
                {Manifest.permission.WRITE_EXTERNAL_STORAGE}, MODE_PRIVATE);

        songList = new ArrayList<String>();

        File[] listFiles = new File(song_Path).listFiles();
        String fileName, extName;
        for (File file : listFiles) {
            fileName = file.getName();
            extName = fileName.substring(fileName.length() - 3);

            if (extName.equals((String) "mp3")) {
                songList.add(fileName);
            }
        }

        songListView = (ListView) findViewById(R.id.SLV);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice, songList);
        songListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        songListView.setAdapter(adapter);
        songListView.setItemChecked(0, true);

        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selected_song = songList.get(i);
                rePlay.setChecked(false);
                rePlay.setEnabled(false);
                btnPlay.setEnabled(true);
            }
        });
        selected_song = songList.get(0);

        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnPause = (Button) findViewById(R.id.btnPause);
        btnStop = (Button) findViewById(R.id.btnStop);
        songName = (TextView) findViewById(R.id.song_Name);
        currentTime = (TextView) findViewById(R.id.current_Time);
        fullTime = (TextView) findViewById(R.id.full_song_Time);
        progress = (SeekBar) findViewById(R.id.song_Progress);
        rePlay = (CheckBox) findViewById(R.id.rePlay);
        song_Player = new MediaPlayer();

        btnPause.setEnabled(false);
        btnStop.setEnabled(false);
        rePlay.setEnabled(false);
        SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");

        rePlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(rePlay.isChecked()) {
                    song_Player.setLooping(true);
                }
                else {
                    song_Player.setLooping(false);
                }
            }
        });

        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) song_Player.seekTo(progress);
            }
            public void onStartTrackingTouch(SeekBar seekBar) { }

            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rePlay.setEnabled(true);
                btnPause.setText("일시 정지");
                song_Player.reset();

                try {
                    song_Player.setDataSource(song_Path + selected_song);
                    song_Player.prepare();
                    song_Player.start();

                    songName.setText(selected_song.substring(0, selected_song.length() - 4));
                    btnPlay.setEnabled(false);
                    btnPause.setEnabled(true);
                    btnStop.setEnabled(true);

                    new Thread() {
                        public void run() {
                            if (song_Player == null) return;
                            progress.setMax(song_Player.getDuration());
                            while (song_Player.isPlaying()) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        progress.setProgress(song_Player.getCurrentPosition());
                                        currentTime.setText(timeFormat.format(song_Player.getCurrentPosition()));
                                        fullTime.setText(timeFormat.format(song_Player.getDuration()));
                                    }
                                });
                                SystemClock.sleep(200);
                            }
                        }
                    }.start();
                } catch (IOException e) {
                }
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(song_Player.isPlaying()) {
                    btnPause.setText("재생");
                    song_Player.pause();
                }
                else {
                    new Thread() {
                        public void run() {
                            if (song_Player == null) return;
                            while (song_Player.isPlaying()) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        progress.setProgress(song_Player.getCurrentPosition());
                                        currentTime.setText(timeFormat.format(song_Player.getCurrentPosition()));
                                    }
                                });
                                SystemClock.sleep(200);
                            }
                        }
                    }.start();
                    song_Player.start();
                    btnPause.setText("일시 정지");
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                song_Player.stop();
                btnPlay.setEnabled(true);
                btnPause.setEnabled(false);
                btnStop.setEnabled(false);

                song_Player.reset();
                songName.setText("노래를 선택해주세요.");
                progress.setProgress(0);
                currentTime.setText("XX:XX");
                fullTime.setText("XX:XX");
                btnPause.setText("일시 정지");
            }
        });

        song_Player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                song_Player.reset();
                btnPlay.setEnabled(true);
                btnPause.setEnabled(false);
                btnStop.setEnabled(false);

                songName.setText("노래를 선택해주세요.");
                progress.setProgress(0);
                currentTime.setText("XX:XX");
                fullTime.setText("XX:XX");
            }
        });
    }
}