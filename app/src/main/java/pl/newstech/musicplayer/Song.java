package pl.newstech.musicplayer;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Created by Bartek on 18.01.2016.
 */

public class Song {

    private long id;
    private String title = String.valueOf(R.string.titleText);
    private String artist = String.valueOf(R.string.artistText);
    private Bitmap cover;

    public long getAlbumIdColumn() {
        return albumIdColumn;
    }

    public void setAlbumIdColumn(long albumIdColumn) {
        this.albumIdColumn = albumIdColumn;
    }

    public static Uri getsArtworkUri() {
        return sArtworkUri;
    }

    private long albumIdColumn;
    final public static Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");

    public Song(long songID, String songTitle, String songArtist, long albumIdColumn/*, Bitmap coverBitmap*/){
        id = songID;
        title = songTitle;
        artist = songArtist;
        this.albumIdColumn = albumIdColumn;
       //cover = coverBitmap;
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
