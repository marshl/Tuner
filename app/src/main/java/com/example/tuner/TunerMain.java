package com.example.tuner;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class TunerMain extends Activity {
    private static RadioMaster radioMasterInstance;
    private ViewPager viewPager;

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
            ActionBar.Tab tab = actionBar.newTab();
            tab.setText(radio.getName());
            tab.setTabListener(tabListener);
            actionBar.addTab(tab);
        }

        actionBar.setSelectedNavigationItem(radioMasterInstance.getCurrentRadio().getIndex());

        this.viewPager.setOnPageChangeListener
                (
                        new ViewPager.SimpleOnPageChangeListener() {
                            @Override
                            public void onPageSelected(int _position) {
                                // When swiping between pages, select the
                                // corresponding tab.
                                getActionBar().setSelectedNavigationItem(_position);

                            }
                        }
                );

        if (TunerAudioControl.getInstance() == null) {
            TunerAudioControl audioControl = new TunerAudioControl(this, radioMasterInstance);
            try {
                audioControl.playNextItem(true);
            } catch (Exception _e) {
                CustomLog.appendException(_e);
                _e.printStackTrace();
                throw new RuntimeException(_e);
            }
        }

        this.onSoundItemChange();
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    public void onPlayPauseButtonClick(View _view) {
        final ImageButton playPauseButton = (ImageButton) this.findViewById(R.id.play_pause_button);

        if (TunerAudioControl.getInstance().getIsPlaying()) {
            TunerAudioControl.getInstance().pause();
            playPauseButton.setImageResource(R.drawable.ic_media_play);
        } else {
            TunerAudioControl.getInstance().resume();
            playPauseButton.setImageResource(R.drawable.ic_media_pause);
        }
    }

    // Do not change syntax
    public void onSkipButtonClick(View _view) {
        try {
            TunerAudioControl.getInstance().playNextItem(false);
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
            TunerAudioControl.getInstance().pause();
            radioMasterInstance.setCurrentRadio(song.getParentStation().getParentRadio());
            radioMasterInstance.getCurrentRadio().setCurrentStation(song.getParentStation());
            TunerAudioControl.getInstance().playFileList(song.getFileList(), true);
            this.onSoundItemChange();
        } catch (Exception _e) {
            CustomLog.appendException(_e);
            _e.printStackTrace();
            throw new RuntimeException(_e);
        }
    }

    public void onSoundItemChange() {
        Log.d("TNR", "onSoundItemChange");
        SoundFileList fileList = TunerAudioControl.getInstance().getFileList();

        final TextView songNameView = (TextView) this.findViewById(R.id.song_name_text);

        String label;
        if (fileList.song != null) {
            label = fileList.song.getName();
        } else {
            label = soundTypeToLabel(fileList.type);
        }
        songNameView.setText(label);

        final TextView songArtistView = (TextView) this.findViewById(R.id.song_artist_text);
        songArtistView.setText(fileList.song != null ? fileList.song.getArtist() : "");
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

    private String soundTypeToLabel(RadioMaster.SOUND_TYPE _soundType) {
        switch (_soundType) {
            case GENERAL:
                return "General";
            case WEATHER:
                return "Weather";
            case ID:
                return "Station ID";
            case TIME:
                return "Time";
            case SONG:
                return "Song";
            case ADVERT:
                return "Advert";
            case NEWS:
                return "News";
            case TO_NEWS:
                return "News Transition";
            case TO_WEATHER:
                return "Weather Transition";
            case TO_ADVERT:
                return "Advert Transition";
            default:
                throw new IllegalArgumentException("Uncaught SOUND_TYPE" + _soundType);
        }
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
