package com.example.tuner;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TunerMain extends Activity {
    private static RadioMaster radioMasterInstance;
    private ViewPager viewPager;

    public TunerAudioControl audioService;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            TunerMain.this.audioService = ((TunerAudioControl.LocalBinder) service).getService();

            TunerMain.this.audioService.context = TunerMain.this;
            TunerMain.this.audioService.radioMaster = TunerMain.this.radioMasterInstance;
            // Tell the user about this for our demo.
            Toast.makeText(TunerMain.this, R.string.local_service_connected,
                    Toast.LENGTH_SHORT).show();

            try {
                TunerMain.this.audioService.playNextItem(true);
            } catch (Exception _e) {
                CustomLog.appendException(_e);
                _e.printStackTrace();
                throw new RuntimeException(_e);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            TunerMain.this.audioService = null;
            Toast.makeText(TunerMain.this, R.string.local_service_disconnected,
                    Toast.LENGTH_SHORT).show();

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tuner_main);

        CustomLog.appendString("Startup");

        if (radioMasterInstance == null) {
            radioMasterInstance = new RadioMaster();
            try {
                String[] radioNames = this.getResources().getStringArray(R.array.radio_array);
                radioMasterInstance.loadRadioDefinitions(radioNames);
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

            for (int i = 0; i < radioMasterInstance.getRadioCount(); ++i) {
                Radio radio = radioMasterInstance.getRadio(i);
                Tab tab = actionBar.newTab();
                tab.setText(radio.getName());
                tab.setTabListener(tabListener);
                actionBar.addTab(tab);
            }

            actionBar.setSelectedNavigationItem(radioMasterInstance.getCurrentRadio().getIndex());
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
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
        this.doUnbindAudioService();
    }

    private void doUnbindAudioService() {
        if (!this.isAudioServiceBound) {
            return;
        }

        this.unbindService(this.serviceConnection);
        this.isAudioServiceBound = false;
    }

    private void doBindAudioService() {
        if (this.isAudioServiceBound) {
            Log.d("TunerMain", "Audio Service already created");
            return;
        }

        Log.d("TunerMain", "Binding audio service");
        Intent bindIntent = new Intent(TunerMain.this, TunerAudioControl.class);
        this.bindService(bindIntent, this.serviceConnection, Context.BIND_AUTO_CREATE);
        this.isAudioServiceBound = true;
    }

    private boolean isAudioServiceBound = false;

    public void onPlayPauseButtonClick(View _view) {
        final ImageButton playPauseButton = (ImageButton) this.findViewById(R.id.play_pause_button);

        if (this.audioService.getIsPlaying()) {
            this.audioService.pause();
            playPauseButton.setImageResource(R.drawable.ic_media_play);
        } else {
            this.audioService.resume();
            playPauseButton.setImageResource(R.drawable.ic_media_pause);
        }
    }

    // Do not change syntax
    public void onSkipButtonClick(View _view) {
        try {
            this.audioService.playNextItem(false);
        } catch (Exception _e) {
            CustomLog.appendException(_e);
            _e.printStackTrace();
            throw new RuntimeException(_e);
        }
    }

    public void selectSong(Song song) {

        int radioIndex = song.getParentStation().getParentRadio().getIndex();
        if (radioIndex != this.viewPager.getCurrentItem()) {
            this.viewPager.setCurrentItem(radioIndex, true);
        }

        try {
            this.audioService.pause();
            radioMasterInstance.setCurrentRadio(song.getParentStation().getParentRadio());
            radioMasterInstance.getCurrentRadio().setCurrentStation(song.getParentStation());
            this.audioService.playSoundList(song.getFileList(), true);
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
        SoundFileList fileList = this.audioService.getSoundFileList();

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
        playPauseButton.setImageResource(this.audioService.getIsPlaying() ? R.drawable.ic_media_pause : R.drawable.ic_media_play);
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
            return RadioFragment.newInstance(radioMasterInstance.getRadio(position));
        }

        @Override
        public int getCount() {
            return radioMasterInstance.getRadioCount();
        }

        @Override
        public CharSequence getPageTitle(int _position) {
            return radioMasterInstance.getRadio(_position).getName();
        }
    }

}
