package com.example.iamas.mp3playerproject;

import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.net.URI;
import java.util.ArrayList;

public class PlayListActivity extends AppCompatActivity {
    private static final String TAG = "Play";
    private TextView txtTitle;
    private GridView gridView;
    // 플레이 리스트의 테이블 명을 저장할 list
    private ArrayList<String> arrTblNames = new ArrayList<>();
    public static MyGridAdapter gAdapter;
    ArrayList<MyData> myPlayList = new ArrayList<>();
    ArrayList<MyData> mySinglePlayList = new ArrayList<>();
    View listDialogView;
    AlertDialog.Builder listDialog;
    RecyclerView recyclerViewPlayList;
    LinearLayoutManager playManager;
    PlayListAdapter playAdapter;
    EditText edtPlayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list);

        getSupportActionBar().setSubtitle(" - my music play list");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.cassette);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3bb9ab7e")));
        getSupportActionBar().setElevation(200);


        txtTitle = findViewById(R.id.txtTitle);
        gridView = findViewById(R.id.gridView);

        callTableName();

        gAdapter = new MyGridAdapter(this, R.layout.single_image, arrTblNames);
        gridView.setAdapter(gAdapter);


    } // end of onCreate

    private void callTableName() {
        MainActivity.db = MainActivity.myDBHelper.getWritableDatabase();
        // 테이블(저장한 플레이 리스트)의 테이블명만 불러오기
        String str = "SELECT name FROM sqlite_master WHERE type='table';";

        Cursor cursorOrder = MainActivity.db.rawQuery(str, null);
        while (cursorOrder.moveToNext()) {
            arrTblNames.add(cursorOrder.getString(cursorOrder.getColumnIndex("name")));
        }
        cursorOrder.close();
    }

    public class MyGridAdapter extends BaseAdapter {
        Context context;
        int layout;
        private ArrayList<String> arrTblNames;
        LayoutInflater layoutInflater;

        public MyGridAdapter(Context context, int layout, ArrayList<String> arrTblNames) {
            this.context = context;
            this.layout = layout;
            this.arrTblNames = arrTblNames;
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return arrTblNames.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = layoutInflater.inflate(layout, null);
            }
            ImageView ivPlaylistImage = convertView.findViewById(R.id.ivPlaylistImage);
            TextView ivPlaylistTitle = convertView.findViewById(R.id.ivPlaylistTitle);

            ivPlaylistTitle.setText(arrTblNames.get(position));
            ivPlaylistImage.setImageResource(R.drawable.cassette);

            // 재생 목록 이미지 클릭 이벤트
            ivPlaylistImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mySinglePlayList.removeAll(mySinglePlayList);
                    MainActivity.db = MainActivity.myDBHelper.getWritableDatabase();
                    String strImage = "SELECT * FROM " + arrTblNames.get(position) + ";";

                    Cursor cursor = MainActivity.db.rawQuery(strImage, null);
                    while (cursor.moveToNext()) {
                        getMyData(Uri.parse(cursor.getString(0)), cursor.getString(1), cursor.getString(2),
                                cursor.getString(3), cursor.getString(4), cursor.getString(5));
                    }
                    cursor.close();

                    listDialogView = View.inflate(PlayListActivity.this, R.layout.play_dialog, null);
                    listDialog = new AlertDialog.Builder(PlayListActivity.this);

                    edtPlayList = listDialogView.findViewById(R.id.edtPlayList);
                    recyclerViewPlayList = listDialogView.findViewById(R.id.recyclerViewPlayList);

                    edtPlayList.setText(arrTblNames.get(position));
                    playManager = new LinearLayoutManager(context);
                    recyclerViewPlayList.setLayoutManager(playManager);
                    playAdapter = new PlayListAdapter(R.layout.list_item, mySinglePlayList);
                    recyclerViewPlayList.setAdapter(playAdapter);

                    listDialog.setView(listDialogView);

                    listDialog.setNegativeButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    listDialog.show();
                }
            });


            ivPlaylistImage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // 정말 삭제할지 물어보는 다이어로그
                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(PlayListActivity.this);
                    alertDialog.setTitle("  Delete");
                    alertDialog.setMessage("이 플레이 리스트를 정말 삭제 하시겠습니까?");
                    alertDialog.setIcon(R.mipmap.delete);

                    alertDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            MainActivity.db = MainActivity.myDBHelper.getWritableDatabase();
                            String strDelete = "DROP TABLE IF EXISTS " + arrTblNames.get(position);
                            MainActivity.db.execSQL(strDelete);

                            arrTblNames.removeAll(arrTblNames);
                            callTableName();
                            gAdapter.notifyDataSetChanged();
                        }
                    });


                    alertDialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertDialog.show();
                    return false;
                }
            });

            return convertView;
        }
    }

    public void getMyData(Uri musicImg, String title, String singer, String duration, String albumId, String musicId) {
        MyData myData = new MyData();
        myData.setMusicImg(musicImg);
        myData.setTitle(title);
        myData.setSinger(singer);
        myData.setDuration(duration);
        myData.setAlbumId(albumId);
        myData.setMusicId(musicId);
        mySinglePlayList.add(myData);
    }


} // main
