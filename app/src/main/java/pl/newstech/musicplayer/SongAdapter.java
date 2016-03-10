package pl.newstech.musicplayer;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by Bartek on 18.01.2016.
 */

public class SongAdapter extends BaseAdapter {

    //song_list list and layout
    private ArrayList<Song> songs;
    private LayoutInflater songInf;
    private Context c;
    //constructor
    public SongAdapter(Context c, ArrayList<Song> theSongs){
        songs = theSongs;
        songInf = LayoutInflater.from(c);
        this.c = c;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map to song_list layout
        LinearLayout songLayout = (LinearLayout)songInf.inflate
                (R.layout.song_list, parent, false);
        //get title and artist views
        TextView songView = (TextView)songLayout.findViewById(R.id.song_title);
        TextView artistView = (TextView)songLayout.findViewById(R.id.song_artist);
        ImageView coverView = (ImageView)songLayout.findViewById(R.id.image_list);
        //get song_list using position
        Song currSong = songs.get(position);
        //get title and artist strings
        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());
        Uri uri = ContentUris.withAppendedId(currSong.getsArtworkUri(), currSong.getAlbumIdColumn());
        Glide.with(c).load(uri).placeholder(R.drawable.default_cover).error(R.drawable.default_cover)
                .crossFade().centerCrop().into(coverView);
        //coverView.setImageBitmap(currSong.getCover());
        //set position as tag
        songLayout.setTag(position);
        return songLayout;
    }

}
