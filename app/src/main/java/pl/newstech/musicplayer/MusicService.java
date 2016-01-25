package pl.newstech.musicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;


/**
 * Created by Bartek on 18.01.2016.
 */

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    //media player
    private MediaPlayer player;
    //song list
    private ArrayList<Song> songs;
    //current position
    private int songCurrPosition;
    //binder
    private final IBinder musicBind = new MusicBinder();
    //title of current song
    private String songTitle;
    //notification id
    private static final int NOTIFY_ID = 1;
    //shuffle flag and random
    private boolean shuffle = false;
    private boolean repeat = false;
    private Random rand;

    public void onCreate(){
        //create the service
        super.onCreate();
        //initialize position
        songCurrPosition = 0;
        //random
        rand = new Random();
        //create player
        player = new MediaPlayer();
        //initialize
        initMusicPlayer();
    }

    public void initMusicPlayer(){
        //set player properties
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //set listeners
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    //pass song list
    public void setList(ArrayList<Song> theSongs){
        songs = theSongs;
    }

    //binder
    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    //activity will bind to service
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    //release resources when unbind
    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    //play_icon a song
    public void playSong(){
        //play_icon
        player.reset();
        //get song
        Song playSong = songs.get(songCurrPosition);

        //get title
        songTitle = playSong.getTitle();
        //get id
        long currentSong = playSong.getID();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currentSong);
        //set the data source
        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }

    //set the song
    public void setSong(int songIndex){
        songCurrPosition = songIndex;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        //check if playback has reached the exit_app of a track
        if(player.getCurrentPosition() > 0){
            mediaPlayer.reset();
            playNext(false);
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        Log.v("MUSIC PLAYER", "Playback Error");
        mediaPlayer.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        //start playback
        mediaPlayer.start();
        //notification
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play_icon)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle(getString(R.string.playText))
                .setContentText(songTitle);
        Notification not = builder.build();
        startForeground(NOTIFY_ID, not);
    }

    //playback methods
    public int getCurrPosition(){
        return player.getCurrentPosition();
    }

    public int getDuration(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int position){
        player.seekTo(position);
    }

    public void go(){
        player.start();
    }

    //skip to previous track
    public void playPrev(boolean byUser){
        if(repeat && !byUser) {

        }
        else {
            songCurrPosition--;
            if(songCurrPosition < 0)
                songCurrPosition = songs.size() - 1;
        }
        playSong();
    }

    //skip to next
    public void playNext(boolean byUser){
        if(repeat && !byUser) {

        }
        else if(shuffle){
            int newSong = songCurrPosition;
            while(newSong == songCurrPosition){
                newSong = rand.nextInt(songs.size());
            }
            songCurrPosition = newSong;
        }
        else {
            songCurrPosition++;
            if(songCurrPosition >= songs.size()) songCurrPosition = 0;
        }
        playSong();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    //toggle shuffle
    public void setShuffle(){
        if(shuffle) {
            shuffle = false;

            Toast.makeText(this, "Shuffle this song on",
                    Toast.LENGTH_SHORT).show();
        }
        else {
            shuffle = true;
            Toast.makeText(this, "Shuffle this song on",
                    Toast.LENGTH_SHORT).show();
        }
    }

    //toggle repeat
    public void setRepeat() {
        if(repeat) {
            repeat = false;
            Toast.makeText(this, "Repeat this song off",
                    Toast.LENGTH_SHORT).show();
        }
        else {
            repeat = true;
            Toast.makeText(this, "Repeat this song on",
                    Toast.LENGTH_SHORT).show();
        }
    }

}
