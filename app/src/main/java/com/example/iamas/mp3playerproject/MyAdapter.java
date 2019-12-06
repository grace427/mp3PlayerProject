package com.example.iamas.mp3playerproject;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.CustomViewHolder> {
    int layout;
    ArrayList<MyData> myDataList;
    public static ArrayList<MyData> myPlayList = new ArrayList<>();
    public static boolean isFavorite = false;


    public MyAdapter(int layout, ArrayList<MyData> myDataList) {
        this.layout = layout;
        this.myDataList = myDataList;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(layout, viewGroup, false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final CustomViewHolder customViewHolder, int position) {
        // 각 아이템에 내용 장착
        if (myDataList.get(position).getMusicImg() == null) {
            customViewHolder.imageView.setBackgroundResource(R.drawable.cassette);
        } else {
            customViewHolder.imageView.setImageURI(myDataList.get(position).getMusicImg());
        }
        customViewHolder.txtTitlePart.setText(myDataList.get(position).getTitle());
        customViewHolder.txtSingerPart.setText(myDataList.get(position).getSinger());
        customViewHolder.txtDurationPart.setText(DateFormat.format("mm:ss", Integer.parseInt(myDataList.get(position).getDuration())));
        customViewHolder.btnFavorite.setTag(position);
        customViewHolder.btnStar.setTag(position);

        // 즐겨찾기 버튼 초기 설정
        customViewHolder.btnFavorite.setVisibility(View.VISIBLE);
        customViewHolder.btnStar.setVisibility(View.GONE);


        // 즐겨찾기 버튼 클릭 -> 선택된 노래들이 myPlaylist에 들어간다
        customViewHolder.btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               int position = (int) v.getTag();
                customViewHolder.btnFavorite.setVisibility(View.GONE);
                customViewHolder.btnStar.setVisibility(View.VISIBLE);
                isFavorite = true;
                myPlayList.add(myDataList.get(position));
            }
        });

        // 즐겨찾기해제 버튼 클릭 -> 선택된 노래들이 myPlaylist에서 지워진다
        customViewHolder.btnStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                customViewHolder.btnFavorite.setVisibility(View.VISIBLE);
                customViewHolder.btnStar.setVisibility(View.GONE);
                isFavorite = false;
                myPlayList.remove(myDataList.get(position));
            }
        });


    } // end of OnBindViewHolder

    @Override
    public int getItemCount() {
        return (myDataList != null) ? myDataList.size() : 0;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView txtTitlePart, txtSingerPart, txtDurationPart;
        public ImageButton btnFavorite, btnStar;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            txtTitlePart = itemView.findViewById(R.id.txtTitlePart);
            txtSingerPart = itemView.findViewById(R.id.txtSingerPart);
            txtDurationPart = itemView.findViewById(R.id.txtDurationPart);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            btnStar = itemView.findViewById(R.id.btnStar);
        }
    }
}
