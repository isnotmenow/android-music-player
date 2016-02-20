package pl.newstech.musicplayer;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.MediaController;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements MediaController.MediaPlayerControl {

    //song_list list variables
    private ArrayList<Song> songList;//list of songs

    //service
    private MusicService musicService;
    private Intent playIntent;

    //binding
    private boolean musicBound = false;

    //controller
    private MusicController controller;

    private ListView songView;

    //activity and playback pause flags
    private boolean paused = false,
            playbackPaused = false;

    private int sortType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        songView = (ListView) findViewById(R.id.song_list);

        songList = new ArrayList<>();
        //get songs from device
        getSongList();

        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        //create and set adapter
        SongAdapter songAdapter = new SongAdapter(this, songList);
        songView.setAdapter(songAdapter);

        //setup controller
        setController();
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            //get service
            musicService = binder.getService();
            //pass list
            musicService.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    //start and bind the service when the activity starts
    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent == null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    //user song_list select
    public void songPicked(View view){
        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();
        if(playbackPaused == true){
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    //method to retrieve song_list info from device
    public void getSongList(){
        //query external audio
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        ContentResolver musicResolver = getContentResolver();
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        //iterate over results if valid
        if(musicCursor  != null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            long albumIdColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);

            Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri, albumIdColumn);

            InputStream in = null;
            try {
                in = musicResolver.openInputStream(uri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap artwork = BitmapFactory.decodeStream(in);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist, artwork));
            }
            while (musicCursor.moveToNext());
        }
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicService != null && musicBound && musicService.isPng())
            return musicService.getCurrPosition();
        else return 0;
    }

    @Override
    public int getDuration() {
        if(musicService != null && musicBound && musicService.isPng())
            return musicService.getDuration();
        else return 0;
    }

    @Override
    public boolean isPlaying() {
        if(musicService != null && musicBound)
            return musicService.isPng();
        return false;
    }

    @Override
    public void pause() {
        playbackPaused = true;
        musicService.pausePlayer();
    }

    @Override
    public void seekTo(int pos) {
        musicService.seek(pos);
    }

    @Override
    public void start() {
        musicService.go();
    }

    //set the controller up
    private void setController(){
        controller = new MusicController(this);
        //set previous and next button listeners
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        //set and show
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }

    private void playNext(){
        musicService.playNext(true);
        if(playbackPaused){
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    private void playPrev(){
        musicService.playPrev(true);
        if(playbackPaused){
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    @Override
    protected void onPause(){
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(paused){
            setController();
            paused = false;
        }
    }

    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicService = null;
        super.onDestroy();
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    finish();
                    stopService(playIntent);
                    musicService = null;
                    System.exit(0);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


        switch (item.getItemId()) {
            case R.id.action_repeat:
                musicService.setRepeat();
                break;
            case R.id.action_shuffle:
                musicService.setShuffle();
                break;
            case R.id.action_settings:
                Intent activityCalled = new Intent(this, SettingsActivity.class);
                final int result = 1;
                activityCalled.putExtra("settingSort", sortType);
                startActivityForResult(activityCalled, result);
                break;
            case R.id.action_exit:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.dialogQuestion)).setPositiveButton(getString(R.string.yesQuestion), dialogClickListener)
                        .setNegativeButton(getString(R.string.noQuestion), dialogClickListener).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void reSortSongList(int sortSongListType) {

        if(sortSongListType == 0) {
            Collections.sort(songList, new Comparator<Song>() {
                public int compare(Song a, Song b) {
                    return a.getTitle().compareTo(b.getTitle());
                }
            });
        }
        else {
            Collections.sort(songList, new Comparator<Song>() {
                public int compare(Song a, Song b) {
                    return a.getArtist().compareTo(b.getArtist());}
            });
        }
        songView.invalidateViews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Get the users name from the previous Activity
        sortType = data.getIntExtra("settingSort", 0);
        reSortSongList(sortType);
    }
}
