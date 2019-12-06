package com.example.iamas.mp3playerproject;

import android.net.Uri;


public class MyData  {

    private Uri musicImg;
    private String title;
    private String singer;
    private String duration;
    private String albumId;
    private String musicId;


    public MyData(){

    }
    public MyData(Uri musicImg) {
        this.musicImg = musicImg;
    }

    public MyData(String albumId) {
        this.albumId = albumId;
    }


    public MyData(Uri musicImg, String title, String singer, String duration, String albumId, String musicId) {
        this.musicImg = musicImg;
        this.title = title;
        this.singer = singer;
        this.duration = duration;
        this.albumId = albumId;
        this.musicId = musicId;
    }

    public Uri getMusicImg() {
        return musicImg;
    }

    public void setMusicImg(Uri musicImg) {
        this.musicImg = musicImg;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getMusicId() {
        return musicId;
    }

    public void setMusicId(String musicId) {
        this.musicId = musicId;
    }
}


