package com.example.tuner;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TunerAudioControl extends Service {
    private boolean isPlaying = false;
    private SoundFileList soundFileList;
    private List<MediaPlayer> mediaPlayerList = new ArrayList<MediaPlayer>();
    private MediaPlayer pausedPlayer;

    private static TunerAudioControl instance;

    public TunerAudioControl() {
        TunerAudioControl.instance = this;
    }

    public static TunerAudioControl getInstance() {
        return TunerAudioControl.instance;
    }

    public void playNextItem(boolean reset) throws IOException {
        RadioMaster.SOUND_TYPE soundType = RadioMaster.getInstance().getRandomSoundType(reset);
        SoundFileList fileList = RadioMaster.getInstance().getNextFileBlock(soundType);
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

        for (int i = 0; i < soundList.getFileCount(); ++i) {
            File file = soundList.getFileAtIndex(i);
            MediaPlayer player = this.mediaPlayerList.get(i);
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
        lastPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer _mediaPlayer) {
                try {
                    TunerAudioControl.this.playNextItem(false);
                } catch (Exception _e) {
                    CustomLog.appendException(_e);
                    _e.printStackTrace();
                    throw new RuntimeException(_e);
                }
            }
        });
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
        if (this.isPlaying || this.pausedPlayer == null) {
            return;
        }

        this.isPlaying = true;
        this.pausedPlayer.start();
        this.pausedPlayer = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        TunerAudioControl getService() {
            return TunerAudioControl.this;
        }
    }

    @Override
    public void onCreate() {
        this.notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        this.showNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        Log.i("LocalService", "Received start id " + startID + ": " + intent);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        this.notificationManager.cancel(this.NOTIFICATION);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
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

            if (this.isResetting && RadioMaster.getInstance().getCurrentRadio().getCurrentStation().getIsFullTrack()) {
                _mediaPlayer.seekTo((int) (Math.random() * _mediaPlayer.getDuration()));
            }
            _mediaPlayer.start();
        }
    }

    public boolean getIsPlaying() {
        return this.isPlaying;
    }

    public SoundFileList getSoundFileList() {
        return this.soundFileList;
    }

    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.local_service_started);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.local_service_label))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .build();

        // Send the notification.
        this.notificationManager.notify(NOTIFICATION, notification);
    }

    private NotificationManager notificationManager;

    private int NOTIFICATION = R.string.local_service_started;

}
