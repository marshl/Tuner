package com.example.tuner;

import android.app.Activity;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SongListFragment extends ListFragment
{
    private TunerMain context;
    private Station station;

    public SongListFragment( TunerMain _context, Station _station )
    {
        this.context = _context;
        this.station = _station;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.setListAdapter( new SongListAdapter( this.context, this.station ) );
    }


    @Override
    public void onAttach( Activity activity )
    {
        super.onAttach(activity);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    @Override
    public void onListItemClick( ListView _listView, View _view, int _position, long _id)
    {
        super.onListItemClick( _listView, _view, _position, _id );
    }
}
