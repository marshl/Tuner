package com.example.tuner;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class RadioFragment extends Fragment
{
    public Activity context;
	public int radioIndex;
	public Radio radio;

	public ListView stationList;

    public RadioFragment()
    {

    }

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState )
	{
		View rootView = inflater.inflate( R.layout.fragment_tuner_main,
				container, false );

		this.stationList = (ListView)rootView.findViewById( R.id.station_list );
		this.stationList.setAdapter( new StationListAdapter( this.context, this ) );
		return rootView;
	}

    @Override
    public void onAttach( Activity _activity )
    {
        super.onAttach( _activity );
        this.context = _activity;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        this.context  = null;
    }
}
