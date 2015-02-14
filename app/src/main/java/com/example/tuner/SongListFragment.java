package com.example.tuner;

import android.app.Activity;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.View;
import android.widget.ListView;

public class SongListFragment extends ListFragment
{
    private static final String RADIO_ID_PARAM = "RADIO";
    private static final String STATION_ID_PARAM = "STATION";

    private OnFragmentInteractionListener interactionListener;

    private Station station;


    public static SongListFragment newInstance( int _radioIndex, int _stationIndex )
    {
        SongListFragment fragment = new SongListFragment();
        Bundle args = new Bundle();
        args.putInt( RADIO_ID_PARAM, _radioIndex );
        args.putInt( STATION_ID_PARAM, _stationIndex );
        fragment.setArguments( args );
        return fragment;
    }

    public SongListFragment()
    {

    }

    @Override
    public void onCreate( Bundle _savedInstanceState )
    {
        super.onCreate( _savedInstanceState );

        if ( this.getArguments() != null )
        {
            int radioIndex   = getArguments().getInt( RADIO_ID_PARAM );
            int stationIndex = getArguments().getInt( STATION_ID_PARAM );
            this.station = RadioMaster.instance.radioList.get( radioIndex ).stationList.get( stationIndex );
        }

        this.setListAdapter( new SongListAdapter( this.station ) );
    }


    @Override
    public void onAttach( Activity _activity )
    {
        super.onAttach( _activity );
        try
        {
            this.interactionListener = (OnFragmentInteractionListener)_activity;
        }
        catch ( ClassCastException _e )
        {
            throw new ClassCastException( _activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        this.interactionListener = null;
    }


    @Override
    public void onListItemClick( ListView _listView, View _view, int _position, long _id )
    {
        super.onListItemClick( _listView, _view, _position, _id);

        if ( this.interactionListener != null )
        {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            this.interactionListener.onFragmentInteraction( _position );
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener
    {
        // TODO: Update argument type and name
        public void onFragmentInteraction( int _position );
    }

}
