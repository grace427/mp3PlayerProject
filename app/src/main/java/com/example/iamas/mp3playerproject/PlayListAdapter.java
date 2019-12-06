package com.example.iamas.mp3playerproject;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.CustomViewHolder> {
    int layout;
    ArrayList<MyData> myPlayList;


    public PlayListAdapter(int layout, ArrayList<MyData> myPlayList) {
        this.layout = layout;
        this.myPlayList = myPlayList;
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
        if (myPlayList.get(position).getMusicImg() == null) {
            customViewHolder.imageView.setBackgroundResource(R.drawable.cassette);
        } else {
            customViewHolder.imageView.setImageURI(myPlayList.get(position).getMusicImg());
        }


        customViewHolder.txtTitlePart.setText(myPlayList.get(position).getTitle());
        customViewHolder.txtSingerPart.setText(myPlayList.get(position).getSinger());
        customViewHolder.txtDurationPart.setText(DateFormat.format("mm:ss", Integer.parseInt(myPlayList.get(position).getDuration())));
        customViewHolder.btnFavorite.setVisibility(View.INVISIBLE);
        customViewHolder.btnStar.setVisibility(View.INVISIBLE);

    } // end of OnBindViewHolder

    @Override
    public int getItemCount() {
        return (myPlayList != null) ? myPlayList.size() : 0;
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
