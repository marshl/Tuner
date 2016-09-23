package com.example.tuner;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TunerMain extends Activity {
    //private static RadioMaster radioMasterInstance;
    private ViewPager viewPager;
    //public TunerAudioControl audioService;
    private PhoneStateListener phoneStateListener;
    private BroadcastReceiver broadcastReceiver;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            TunerAudioControl audioControl = ((TunerAudioControl.LocalBinder) service).getService();
            Log.d("onServiceConnected", "Connection to TunerAudioControl has been made");
        }

        public void onServiceDisconnected(ComponentName className) {
            Toast.makeText(TunerMain.this, R.string.local_service_disconnected,
                    Toast.LENGTH_SHORT).show();

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tuner_main);

        CustomLog.appendString("Startup");

        if (RadioMaster.getInstance() == null) {
            RadioMaster radioMaster = new RadioMaster();
            try {
                String[] radioNames = this.getResources().getStringArray(R.array.radio_array);
                radioMaster.loadRadioDefinitions(radioNames);
            } catch (Exception _e) {
                CustomLog.appendException(_e);
                _e.printStackTrace();
                throw new RuntimeException(_e);
            }
        }

        RadioPagerAdapter radioPagerAdapter = new RadioPagerAdapter(this.getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        this.viewPager = (ViewPager) this.findViewById(R.id.radio_view_pager);
        this.viewPager.setAdapter(radioPagerAdapter);

        final ActionBar actionBar = this.getActionBar();
        if (actionBar != null) {
            // Specify that tabs should be displayed in the action bar.
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);


            // Create a tab listener that is called when the user changes tabs.
            ActionBar.TabListener tabListener = new ActionBar.TabListener() {
                @Override
                public void onTabSelected(Tab _tab, FragmentTransaction _ft) {
                    viewPager.setCurrentItem(_tab.getPosition());
                }

                @Override
                public void onTabUnselected(Tab _tab, FragmentTransaction _ft) {
                    // hide the given tab
                }

                @Override
                public void onTabReselected(Tab tab, FragmentTransaction ft) {
                    // TODO Auto-generated method stub
                }
            };

            for (int i = 0; i < RadioMaster.getInstance().getRadioCount(); ++i) {
                Radio radio = RadioMaster.getInstance().getRadio(i);
                Tab tab = actionBar.newTab();
                tab.setText(radio.getName());
                tab.setTabListener(tabListener);
                actionBar.addTab(tab);
            }
        }

        this.viewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int _position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        getActionBar().setSelectedNavigationItem(_position);

                    }
                }
        );

        this.doBindAudioService();

        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        this.phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        TunerMain.this.doPause();
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        break;
                }
                Log.d("TunerMain", "OnCellStateChanged: " + state);
            }
        };

        telephonyManager.listen(this.phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE); // Registers a listener object to receive notification of changes in specified telephony states.

        this.broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("BroadcastReceiver", "Received event " + intent.getAction());
                if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                    Log.d("Broadcastreceiver", "Received headset message, pausing");
                    TunerMain.this.doPause();
                }
            }
        };

        this.registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));

        //AudioManager audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onDestroy() {
        super.onDestroy();
        this.doUnbindAudioService();
        this.unregisterReceiver(this.broadcastReceiver);
    }

    private void doUnbindAudioService() {
        if (!this.isAudioServiceBound()) {
            return;
        }

        //this.unbindService(this.serviceConnection);
    }

    private void doBindAudioService() {
        if (this.isAudioServiceBound()) {
            Log.d("TunerMain", "Audio Service already created");
            return;
        }

        Log.d("TunerMain", "Binding audio service");
        Intent bindIntent = new Intent(TunerMain.this, TunerAudioControl.class);
        this.bindService(bindIntent, this.serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private boolean isAudioServiceBound() {
        return TunerAudioControl.getInstance() != null;//this.isMyServiceRunning(TunerAudioControl.class);
    }

    public void onPlayPauseButtonClick(View view) {
        this.doPause();
    }

    private void doPause()
    {
        final ImageButton playPauseButton = (ImageButton) this.findViewById(R.id.play_pause_button);

        final RadioMaster radioMaster = RadioMaster.getInstance();
        final TunerAudioControl audioService = TunerAudioControl.getInstance();

        if (radioMaster.getCurrentRadio() == null) {
            return;
        }

        if (audioService.getIsPlaying()) {
            audioService.pause();
            playPauseButton.setImageResource(R.drawable.ic_media_play);
        } else {
            audioService.resume();
            playPauseButton.setImageResource(R.drawable.ic_media_pause);
        }
    }

    // Do not change syntax
    public void onSkipButtonClick(View _view) {
        try {
            if (RadioMaster.getInstance().getCurrentRadio() != null) {
                TunerAudioControl.getInstance().playNextItem(false);
            }
        } catch (Exception _e) {
            CustomLog.appendException(_e);
            _e.printStackTrace();
            throw new RuntimeException(_e);
        }

        this.onSoundItemChange();
    }

    public void selectSong(Song song) {

        int radioIndex = song.getParentStation().getParentRadio().getIndex();
        if (radioIndex != this.viewPager.getCurrentItem()) {
            this.viewPager.setCurrentItem(radioIndex, true);
        }

        try {
            TunerAudioControl.getInstance().pause();
            RadioMaster.getInstance().setCurrentRadio(song.getParentStation().getParentRadio());
            RadioMaster.getInstance().getCurrentRadio().setCurrentStation(song.getParentStation());
            TunerAudioControl.getInstance().playSoundList(song.getFileList(), true);
            song.getParentStation().pushSongToEnd(song);
            this.onSoundItemChange();
        } catch (Exception _e) {
            CustomLog.appendException(_e);
            _e.printStackTrace();
            throw new RuntimeException(_e);
        }
    }

    public void onSoundItemChange() {
        Log.d("TNR", "onSoundItemChange");
        SoundFileList fileList = TunerAudioControl.getInstance().getSoundFileList();

        final TextView songNameView = (TextView) this.findViewById(R.id.song_name_text);

        String label;
        if (fileList.getSong() != null) {
            label = fileList.getSong().getName();
        } else {
            label = RadioMaster.soundTypeToLabel(fileList.getSoundType());
        }
        songNameView.setText(label);

        final TextView songArtistView = (TextView) this.findViewById(R.id.song_artist_text);
        songArtistView.setText(fileList.getSong() != null ? fileList.getSong().getArtist() : "");
        songArtistView.invalidate();
        songNameView.invalidate();

        final ImageButton playPauseButton = (ImageButton) this.findViewById(R.id.play_pause_button);
        playPauseButton.setImageResource(TunerAudioControl.getInstance().getIsPlaying() ? R.drawable.ic_media_pause : R.drawable.ic_media_play);
        playPauseButton.invalidate();

        this.viewPager.invalidate();

        for (int i = 0; i < this.viewPager.getChildCount(); ++i) {
            View view = this.viewPager.getChildAt(i);
            ListView stationList = (ListView) view.findViewById(R.id.station_list);
            stationList.invalidateViews();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    public class RadioPagerAdapter extends FragmentPagerAdapter {
        public RadioPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return RadioFragment.newInstance(RadioMaster.getInstance().getRadio(position));
        }

        @Override
        public int getCount() {
            return RadioMaster.getInstance().getRadioCount();
        }

        @Override
        public CharSequence getPageTitle(int _position) {
            return RadioMaster.getInstance().getRadio(_position).getName();
        }
    }

    /*private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }*/

}
