package com.example.tuner;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TunerAudioControl {
    private static TunerAudioControl instance;
    private final RadioMaster radioMaster;
    private final TunerMain context;
    private boolean isPlaying = true;
    private SoundFileList soundFileList;
    private List<MediaPlayer> mediaPlayerList;
    private final OnCompletionListener onSongFinishedListener;
    private MediaPlayer pausedPlayer;

    public TunerAudioControl(TunerMain _context, RadioMaster radioMaster) {
        instance = this;
        this.mediaPlayerList = new ArrayList<MediaPlayer>();
        this.context = _context;
        this.radioMaster = radioMaster;

        this.onSongFinishedListener = new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer _mediaPlayer) {
                try {
                    TunerAudioControl.instance.playNextItem(false);
                } catch (Exception _e) {
                    CustomLog.appendException(_e);
                    _e.printStackTrace();
                    throw new RuntimeException(_e);
                }
            }
        };
    }

    public void playNextItem(boolean reset) throws IOException {
        SoundFileList fileList = this.radioMaster.getNextFileBlock(this.radioMaster.getRandomSoundType(reset));
        this.playSoundList(fileList, reset);
    }

    public void playSoundList(SoundFileList soundList, boolean reset) throws IOException {
        this.isPlaying = true;
        this.pausedPlayer = null;
        this.soundFileList = soundList;

        for (MediaPlayer player : this.mediaPlayerList) {
            player.release();
        }

        this.mediaPlayerList = new ArrayList<MediaPlayer>();

        while (this.mediaPlayerList.size() < soundList.getFileCount()) {
            MediaPlayer player = new MediaPlayer();
            player.setVolume(0.5f, 0.5f);
            this.mediaPlayerList.add(player);
        }
        Log.d("TNR", "Type: " + soundList.getSoundType());
        Log.d("TNR", soundList.getSong() != null ? soundList.getSong().getName() : "No song");
        Log.d("TNR", "File Count: " + soundList.getFileCount());
        Log.d("TNR", "Player Count: " + this.mediaPlayerList.size());
        for (int i = 0; i < soundList.getFileCount(); ++i) {
            File file = soundList.getFileAtIndex(i);
            MediaPlayer player = this.mediaPlayerList.get(i);
            Log.d("TNR", "File exists: " + file.exists());
            Log.d("TNR", "File: " + file != null ? file.toString() : "NULL");
            String path = file.getAbsolutePath();
            player.setDataSource(path);
        }

        MediaPlayer firstPlayer = this.mediaPlayerList.get(0);
        firstPlayer.setOnPreparedListener(new TunerOnPreparedListener(this, reset));

        for (MediaPlayer player : this.mediaPlayerList) {
            player.prepare();
        }

        for (int i = 0; i < this.mediaPlayerList.size() - 1; ++i) {
            MediaPlayer player = this.mediaPlayerList.get(i);
            MediaPlayer nextPlayer = this.mediaPlayerList.get(i + 1);
            player.setNextMediaPlayer(nextPlayer);
        }

        MediaPlayer lastPlayer = this.mediaPlayerList.get(this.mediaPlayerList.size() - 1);
        lastPlayer.setOnCompletionListener(this.onSongFinishedListener);

        this.context.onSoundItemChange();
    }

    public void pause() {
        this.isPlaying = false;
        this.pausedPlayer = null;

        for (MediaPlayer player : this.mediaPlayerList) {
            if (player.isPlaying()) {
                this.pausedPlayer = player;
                player.pause();
                break;
            }
        }
    }

    public void resume() {
        if (this.isPlaying) {
            return;
        }

        this.isPlaying = true;

        assert (this.pausedPlayer != null);

        this.pausedPlayer.start();
        this.pausedPlayer = null;
    }

    public class TunerOnPreparedListener implements OnPreparedListener {
        private final TunerAudioControl audioControl;
        private final boolean isResetting;

        public TunerOnPreparedListener(TunerAudioControl audioControl, boolean reset) {
            this.audioControl = audioControl;
            this.isResetting = reset;
        }

        @Override
        public void onPrepared(MediaPlayer _mediaPlayer) {

            if (this.isResetting && this.audioControl.radioMaster.getCurrentRadio().getCurrentStation().getIsFullTrack()) {
                _mediaPlayer.seekTo((int) (Math.random() * _mediaPlayer.getDuration()));
            }
            _mediaPlayer.start();
        }
    }

    public boolean getIsPlaying() {
        return this.isPlaying;
    }

    public static TunerAudioControl getInstance() {
        return instance;
    }

    public SoundFileList getSoundFileList() {
        return this.soundFileList;
    }
}
