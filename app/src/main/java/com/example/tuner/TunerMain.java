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
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class TunerMain extends Activity implements SongListFragment.OnFragmentInteractionListener
{
	private RadioPagerAdapter radioPagerAdapter;
	private ViewPager viewPager;
	
	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_tuner_main );
		
		if ( RadioMaster.instance == null )
		{
			RadioMaster radioMaster = new RadioMaster( this );
			try
			{
				radioMaster.LoadRadioDefinitions();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		this.radioPagerAdapter = new RadioPagerAdapter( this.getFragmentManager() );

		// Set up the ViewPager with the sections adapter.
		this.viewPager = (ViewPager)this.findViewById( R.id.radio_view_pager );
		this.viewPager.setAdapter( this.radioPagerAdapter );
		
		final ActionBar actionBar = this.getActionBar();
		// Specify that tabs should be displayed in the action bar.
	    actionBar.setNavigationMode( ActionBar.NAVIGATION_MODE_TABS );

	    // Create a tab listener that is called when the user changes tabs.
	    ActionBar.TabListener tabListener = new ActionBar.TabListener()
	    {
	    	@Override
	        public void onTabSelected( Tab _tab, FragmentTransaction _ft )
	        {
	        	viewPager.setCurrentItem( _tab.getPosition() );
	        }

	    	@Override
	        public void onTabUnselected( Tab _tab, FragmentTransaction _ft )
	        {
	            // hide the given tab
	        }

			@Override
			public void onTabReselected( Tab tab, FragmentTransaction ft )
			{
				// TODO Auto-generated method stub				
			}
	    };

	    for ( Radio radio : RadioMaster.instance.radioList )
	    {
	    	ActionBar.Tab tab = actionBar.newTab();
	    	tab.setText( radio.name );
            tab.setTabListener( tabListener );
	        actionBar.addTab( tab );
	    }
	    
	    this.viewPager.setOnPageChangeListener
	    (
	        new ViewPager.SimpleOnPageChangeListener()
	        {
	            @Override
	            public void onPageSelected( int _position )
	            {
	                // When swiping between pages, select the
	                // corresponding tab.
	                getActionBar().setSelectedNavigationItem( _position );
	                
	            }
	        }
	    );
		
	    if ( TunerAudioControl.instance == null )
	    {
		    TunerAudioControl audioControl = new TunerAudioControl( this );
			try
			{
				audioControl.playNextItem();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
	    }
	    
	    this.onSoundItemChange();
	}

    protected void onResume()
    {
        super.onResume();
        RadioMaster.instance.context = this;
    }

    protected void onPause()
    {
        super.onPause();
        RadioMaster.instance.context = this;
    }

    protected void onDestroy()
    {
        RadioMaster.instance.context = this;
        super.onDestroy();
    }

	public void onPlayPauseButtonClick( View _view )
	{
		final ImageButton playPauseButton = (ImageButton)this.findViewById( R.id.play_pause_button );
		
		if ( TunerAudioControl.instance.isPlaying )
		{
			TunerAudioControl.instance.pause();
			playPauseButton.setImageResource( R.drawable.ic_media_play );
		}
		else
		{
			TunerAudioControl.instance.resume();
			playPauseButton.setImageResource( R.drawable.ic_media_pause );
		}
	}
	
	public void onSkipButtonClick( View _view )
	{
		try
		{
			TunerAudioControl.instance.playNextItem();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

    @Override
    public void onFragmentInteraction( int _songIndex )
    {

    }

    public class RadioPagerAdapter extends FragmentPagerAdapter
	{
		public RadioPagerAdapter( FragmentManager fm )
		{
			super( fm );
		}

		@Override
		public Fragment getItem( int _position )
		{
			return RadioFragment.newInstance( _position );
		}

		@Override
		public int getCount()
		{
			return RadioMaster.instance.radioList.size();
		}

		@Override
		public CharSequence getPageTitle( int _position )
		{
			return RadioMaster.instance.radioList.get( _position ).name;
		}
	}

	public void onSoundItemChange()
	{
		SoundFileList fileList = TunerAudioControl.instance.fileList;
		
		final TextView songNameView = (TextView)this.findViewById( R.id.song_name_text );
		songNameView.setText( fileList.song != null ? fileList.song.name : "" );
		
		final TextView songArtistView = (TextView)this.findViewById( R.id.song_artist_text );
		songArtistView.setText( fileList.song != null ? fileList.song.artist : "" );
		
		final ImageButton playPauseButton = (ImageButton)this.findViewById( R.id.play_pause_button );
		playPauseButton.setImageResource( TunerAudioControl.instance.isPlaying ? R.drawable.ic_media_pause : R.drawable.ic_media_play );
		
		this.viewPager.invalidate();
		
		for ( int i = 0; i < this.viewPager.getChildCount(); ++i )
		{
			View view = this.viewPager.getChildAt( i );
			ListView stationList = (ListView)view.findViewById( R.id.station_list );
			stationList.invalidateViews();
		}
	}
}
