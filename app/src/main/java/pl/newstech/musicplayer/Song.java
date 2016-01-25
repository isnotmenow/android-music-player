package pl.newstech.musicplayer;

import android.graphics.Bitmap;

/**
 * Created by Bartek on 18.01.2016.
 */

public class Song {

    private long id;
    private String title = String.valueOf(R.string.titleText);
    private String artist = String.valueOf(R.string.artistText);
    private Bitmap cover;

    public Song(long songID, String songTitle, String songArtist, Bitmap coverBitmap){
        id = songID;
        title = songTitle;
        artist = songArtist;
        cover = coverBitmap;
    }

    public long getID() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getArtist() {
        return artist;
    }
    public Bitmap getCover() {
        return cover;
    }

}
