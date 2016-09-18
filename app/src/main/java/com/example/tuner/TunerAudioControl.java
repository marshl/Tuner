package com.example.tuner;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;

import java.io.IOException;

public class TunerAudioControl {
    private static TunerAudioControl instance;
    private final RadioMaster radioMaster;
    private final TunerMain context;
    private boolean isPlaying = true;
    private SoundFileList fileList;
    private MediaPlayer mainPlayer;
    private MediaPlayer introPlayer;
    private MediaPlayer outroPlayer;
    private final OnCompletionListener onSongFinishedListener;
    private boolean introWasPlaying = false;
    private boolean mainWasPlaying = false;
    private boolean outroWasPlaying = false;

    public TunerAudioControl(TunerMain _context, RadioMaster radioMaster) {
        instance = this;
        this.context = _context;
        this.radioMaster = radioMaster;
        this.mainPlayer = new MediaPlayer();
        this.introPlayer = new MediaPlayer();
        this.outroPlayer = new MediaPlayer();
        this.outroPlayer.setVolume(0.5f, 0.5f);
        this.introPlayer.setVolume(0.5f, 0.5f);
        this.mainPlayer.setVolume(0.5f, 0.5f);

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
        this.playSound(this.radioMaster.getRandomSoundType(reset), reset);
    }

    private void playSound(RadioMaster.SOUND_TYPE _soundType, boolean reset) throws IOException {
        // Unpause and release
        this.isPlaying = true;
        this.mainPlayer.release();
        if (this.introPlayer != null) {
            this.introPlayer.release();
        }

        if (this.outroPlayer != null) {
            this.outroPlayer.release();
        }
        this.introPlayer = this.outroPlayer = null;

        // Play next item
        SoundFileList fileList = this.radioMaster.getNextFileBlock(_soundType);
        if (fileList != null) {
            this.playFileList(fileList, reset);
            // Notify UI
            this.context.onSoundItemChange();
        }
    }

    public void playFileList(SoundFileList _fileList, boolean reset) throws IOException {
        this.fileList = _fileList;

        this.mainPlayer = new MediaPlayer();
        this.introPlayer = null;
        this.outroPlayer = null;

        if (_fileList.introFile != null) {
            this.introPlayer = new MediaPlayer();
            this.introPlayer.setDataSource(_fileList.introFile.toString());
            this.introPlayer.prepare();

            // The intro is the start of the song (in both sequence and overlay mode)
            this.introPlayer.setOnPreparedListener(new TunerOnPreparedListener(this.radioMaster, reset));
        }

        this.mainPlayer.setDataSource(_fileList.mainFile.toString());
        this.mainPlayer.prepare();

        // The main is the start of the song if in overlay mode or there is no intro
        if (_fileList.introFile == null || _fileList.usesOverlay) {
            this.mainPlayer.setOnPreparedListener(new TunerOnPreparedListener(this.radioMaster, reset));
        }

        if (_fileList.outroFile != null) {
            this.outroPlayer = new MediaPlayer();
            this.outroPlayer.setDataSource(_fileList.outroFile.toString());
            this.outroPlayer.prepare();

            // The song ends after the outro if in sequence mode
            if (!_fileList.usesOverlay) {
                this.outroPlayer.setOnCompletionListener(this.onSongFinishedListener);
            }
        }

        // The song ends after main if in sequence mode or no outro
        if (_fileList.outroFile == null || _fileList.usesOverlay) {
            this.mainPlayer.setOnCompletionListener(this.onSongFinishedListener);
        }

        // When in sequence mode, get the players to player after the other
        if (!_fileList.usesOverlay) {
            if (this.introPlayer != null) {
                this.introPlayer.setNextMediaPlayer(this.mainPlayer);
            }

            if (this.outroPlayer != null) {
                this.mainPlayer.setNextMediaPlayer(this.outroPlayer);
            }
        }
    }

    public void pause() {
        this.isPlaying = false;

        this.introWasPlaying = this.introPlayer != null && this.introPlayer.isPlaying();
        if (this.introWasPlaying) {
            this.introPlayer.pause();
        }

        this.mainWasPlaying = this.mainPlayer != null && this.mainPlayer.isPlaying();
        if (this.mainWasPlaying) {
            this.mainPlayer.pause();
        }

        this.outroWasPlaying = this.outroPlayer != null && this.outroPlayer.isPlaying();
        if (this.outroWasPlaying) {
            this.outroPlayer.pause();
        }
    }

    public void resume() {
        this.isPlaying = true;

        if (this.introWasPlaying) {
            this.introPlayer.start();
        }

        if (this.mainWasPlaying) {
            this.mainPlayer.start();
        }

        if (this.outroWasPlaying) {
            this.outroPlayer.start();
        }
    }

    public class TunerOnPreparedListener implements OnPreparedListener {
        private final RadioMaster radioMaster;
        private final boolean isResetting;

        public TunerOnPreparedListener(RadioMaster radioMaster, boolean reset) {
            this.radioMaster = radioMaster;
            this.isResetting = reset;
        }

        @Override
        public void onPrepared(MediaPlayer _mediaPlayer) {
            if (this.isResetting && this.radioMaster.getCurrentRadio().getCurrentStation().getIsFullTrack()) {
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

    public SoundFileList getFileList() {
        return this.fileList;
    }
}
