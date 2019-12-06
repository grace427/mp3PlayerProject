package com.example.iamas.mp3playerproject;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Parcelable;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public RecyclerView recyclerViewMain, recyclerViewPlayList;
    private ImageButton btnPlayMain, btnPauseMain, btnMyPlayList, btnPrevious, btnNext;
    private TextView txtTitleMain, txtSingerMain, txtTimeMain;
    private SeekBar seekBarMain;
    public static EditText edtPlayList;
    public static View dialogView;

    private MediaPlayer mediaPlayer;
    private MyAdapter adapter;
    public PlayListAdapter playAdapter;
    private LinearLayoutManager manager;
    public LinearLayoutManager playManager;
    public static int position;
    public static MyDBHelper myDBHelper;
    public static SQLiteDatabase db;

    public static AlertDialog.Builder dialog;

    private ArrayList<MyData> myDataList = new ArrayList<>();
    public static ArrayList<MyData> myPlayList;

    public static String selectedMP3;
    public static String selectedSinger;
    boolean isPlaying = false;
    boolean itemTouch = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.cassette);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3bb9ab7e")));
        getSupportActionBar().setElevation(200);

        recyclerViewMain = findViewById(R.id.recyclerViewMain);
        btnPlayMain = findViewById(R.id.btnPlayMain);
        btnPauseMain = findViewById(R.id.btnPauseMain);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnMyPlayList = findViewById(R.id.btnMyPlayList);
        txtTitleMain = findViewById(R.id.txtTitleMain);
        txtSingerMain = findViewById(R.id.txtSingerMain);
        txtTimeMain = findViewById(R.id.txtTimeMain);
        seekBarMain = findViewById(R.id.seekBarMain);

        // 내부 저장소 접근 권한 요청
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MODE_PRIVATE);

        mediaPlayer = new MediaPlayer();
        myDBHelper = new MyDBHelper(MainActivity.this);

        // 메인창의 recyclerView에 LinearlayoutManager 장착
        manager = new LinearLayoutManager(this);
        recyclerViewMain.setLayoutManager(manager);

        // 메인창의 recyclerView에 adapter 장착
        adapter = new MyAdapter(R.layout.list_item, myDataList);
        recyclerViewMain.setAdapter(adapter);

        // 재생, 일시정지 버튼 초기화
        buttonInit(View.VISIBLE, View.GONE);

        // seekBar change Listener
        seekBarMain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBarMain.getMax() == progress) {
                    buttonInit(View.VISIBLE, View.GONE);
                    isPlaying = false;
                    mediaPlayer.stop();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isPlaying = false;
                mediaPlayer.pause();
            }

            // seekBar 드래그를 멈추면 멈춘 위치의 seekBar값을 찾아서 거기부터 음악을 시작한다
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isPlaying = true;
                mediaPlayer.seekTo(seekBarMain.getProgress());
                mediaPlayer.start();
                // seekBar의 변화하는 값을 받기 위한 thread
                new MyThread().start();
            }
        });

        // 버튼 이벤트 ---------------------------- //
        btnPlayMain.setOnClickListener(this);
        btnPauseMain.setOnClickListener(this);
        btnPrevious.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnMyPlayList.setOnClickListener(this);

        btnMyPlayList.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlayListActivity.class);
                startActivity(intent);
                return false;
            }
        });

        // 내부저장소의 모든 음악 파일을 myDataList에 담는다
        getPlayList();

        // 리사이클러 뷰 터치 리스너
        recyclerViewMain.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
                if (MotionEvent.ACTION_UP == motionEvent.getAction() && itemTouch) {
                    View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                    position = recyclerView.getChildAdapterPosition(child);

                    selectedMP3 = myDataList.get(position).getTitle();
                    selectedSinger = myDataList.get(position).getSinger();
                    playMusic(myDataList.get(position));
                } else if (MotionEvent.ACTION_DOWN == motionEvent.getAction()) {
                    itemTouch = true;
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean b) {

            }
        });

        // 자동 재생
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (position + 1 < myDataList.size()) {
                    position++;
                    playMusic(myDataList.get(position));
                }
            }
        });

    }// end of onCreate

    // 재생, 일시정지 버튼 초기화
    private void buttonInit(int play, int pause) {
        btnPlayMain.setVisibility(play);
        btnPauseMain.setVisibility(pause);
    }

    // 내부저장소의 모든 음악 파일을 myDataList에 담는다
    private void getPlayList() {
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"; // 음악 파일이면 1을 돌려준다
        final String[] projection = new String[]{
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA
        };
        final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";

        Cursor cursor = null;
        try {
            cursor = getBaseContext().getContentResolver().query(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection, selection, null, sortOrder);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                    Uri uri = ContentUris.withAppendedId(sArtworkUri, Integer.valueOf(cursor.getString(0)));

                    MyData myData = new MyData();
                    myData.setMusicImg(uri);
                    myData.setAlbumId(cursor.getString(0));
                    myData.setTitle(cursor.getString(1));
                    myData.setSinger(cursor.getString(2));
                    myData.setDuration(cursor.getString(3));
                    myData.setMusicId(cursor.getString(4));

                    myDataList.add(myData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    // 재생, 일시정지, 뒤로가기, 앞으로 가기, my Play list 버튼 클릭 이벤트
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPlayMain:
                isPlaying = true;
                new MyThread().start();
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
                mediaPlayer.start();
                buttonInit(View.GONE, View.VISIBLE);
                break;
            case R.id.btnPauseMain:
                buttonInit(View.VISIBLE, View.GONE);
                mediaPlayer.pause();
                isPlaying = false;
                break;
            case R.id.btnPrevious:
                if (position - 1 >= 0) {
                    position--;
                    playMusic(myDataList.get(position));
                    seekBarMain.setProgress(0);
                }
                break;
            case R.id.btnNext:
                if (position + 1 < myDataList.size()) {
                    position++;
                    playMusic(myDataList.get(position));
                    seekBarMain.setProgress(0);
                }
                break;
            case R.id.btnMyPlayList:

                // 즐겨찾기에 선택된 음악들을 myPlaylist에 담아서 다이얼로그에 뿌려준다
                dialogView = View.inflate(MainActivity.this, R.layout.play_dialog, null);
                dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setIcon(R.mipmap.queue);
                dialog.setTitle("  Create your own play list");

                recyclerViewPlayList = dialogView.findViewById(R.id.recyclerViewPlayList);

                playManager = new LinearLayoutManager(this);
                recyclerViewPlayList.setLayoutManager(playManager);
                playAdapter = new PlayListAdapter(R.layout.list_item, MyAdapter.myPlayList);
                recyclerViewPlayList.setAdapter(playAdapter);

                playAdapter.notifyDataSetChanged();
                dialog.setView(dialogView);

                // 저장을 누르면 사용자가 입력한 제목의 테이블이 생성되고 myPlaylist(즐겨찾기 선택한) 내용이 테이블에 insert되어 db에 들어감
                dialog.setPositiveButton("저장", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        edtPlayList = dialogView.findViewById(R.id.edtPlayList);

                        db = myDBHelper.getWritableDatabase();

                        try {
                            String str = "CREATE TABLE " + edtPlayList.getText().toString().trim() + "("
                                    + "uri CHAR(50),"
                                    + " title CHAR(50),"
                                    + " singer CHAR(50),"
                                    + " duration CHAR(50),"
                                    + " albumID CHAR(50),"
                                    + " musicID CHAR(10));";
                            db.execSQL(str);
                        } catch (SQLiteException e) {
                            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                            alertDialog.setTitle("  Notification");
                            alertDialog.setMessage("플레이 리스트 이름이 중복됩니다. 다른 이름을 입력해 주세요!");
                            alertDialog.setIcon(R.mipmap.notification);
                            alertDialog.setNegativeButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            alertDialog.show();
                        }

                        for (MyData myData : MyAdapter.myPlayList) {
                            String strInsert = "INSERT INTO " + edtPlayList.getText().toString().trim() + " values('"
                                    + myData.getMusicImg().toString() + "' , '"
                                    + myData.getTitle() + "' , '"
                                    + myData.getSinger() + "' , '"
                                    + myData.getDuration() + "' , '"
                                    + myData.getAlbumId() + "' , '"
                                    + myData.getMusicId() + "');";
                            db.execSQL(strInsert);
                        }
                    }
                });

                dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                break;
        }
    } // end of onClick


    // 음악 재생
    public void playMusic(MyData myData) {
        try {
            seekBarMain.setProgress(0);
            txtTitleMain.setText(myData.getTitle());
            txtSingerMain.setText(myData.getSinger());
            Uri musicURI = Uri.withAppendedPath(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + myData.getMusicId());
            mediaPlayer.reset();
            mediaPlayer.setDataSource(this, musicURI);
            mediaPlayer.prepare();
            mediaPlayer.start();

            if (mediaPlayer.isPlaying()) {
                buttonInit(View.GONE, View.VISIBLE);
            } else {
                buttonInit(View.VISIBLE, View.GONE);
            }

            // SeekBar의 값 변화를 받기 위한 runOnUiThread
            Thread thread = new Thread() {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

                @Override
                public void run() {
                    if (mediaPlayer == null) {
                        return;
                    }
                    while (mediaPlayer.isPlaying()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                seekBarMain.setMax(mediaPlayer.getDuration());
                                seekBarMain.setProgress(mediaPlayer.getCurrentPosition());
                                txtTimeMain.setText(simpleDateFormat.format(mediaPlayer.getCurrentPosition()));
                            }
                        });  // runOnUIThread()
                        SystemClock.sleep(200);
                    }// end of while
                }
            };
            thread.start();
        } catch (Exception e) {
            Log.d("A", e.getMessage());
        }
    }

    // 어플을 종료하면 mediaPlayer와 db를 닫아준다
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        db.close();
    }


    class MyThread extends Thread {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

        @Override
        public void run() {
            if (mediaPlayer == null) {
                return;
            }
            while (isPlaying) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        seekBarMain.setMax(mediaPlayer.getDuration());
                        seekBarMain.setProgress(mediaPlayer.getCurrentPosition());
                        txtTimeMain.setText(simpleDateFormat.format(mediaPlayer.getCurrentPosition()));
                    }
                });
                SystemClock.sleep(200);
            }// end of while
        }
    }

} // end of MainActivity
