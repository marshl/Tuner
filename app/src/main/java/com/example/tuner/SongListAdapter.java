package com.example.tuner;

import android.app.Activity;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

/**
 * Created by Liam on 13/02/2015.
 */
public class SongListAdapter implements ListAdapter
{
    private Activity context;
    private Station station;

    public SongListAdapter( Station _station )
    {
        this.context = RadioMaster.instance.context;
        this.station = _station;
    }

    @Override
    public boolean areAllItemsEnabled()
    {
        return false;
    }

    @Override
    public boolean isEnabled( int _i )
    {
        return false;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver)
    {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver)
    {

    }

    @Override
    public int getCount()
    {
        return this.station.getSongCount();
    }

    @Override
    public Object getItem( int _i )
    {
        return this.station.getSongCount();
    }

    @Override
    public long getItemId(int i)
    {
        return i;
    }

    @Override
    public boolean hasStableIds()
    {
        return false;
    }

    @Override
    public View getView( int _position, View _convertView, ViewGroup _parent )
    {
        if ( _convertView == null )
        {
            LayoutInflater layoutInflater = this.context.getLayoutInflater();
            _convertView = layoutInflater.inflate( R.layout.song_list_item, _parent, false );
        }

        final Song song = this.station.getSongAtIndex( _position );

        final TextView nameTextView = (TextView)_convertView.findViewById( R.id.song_item_song_name );
        nameTextView.setText( song.name );

        return _convertView;
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty()
    {
        return !this.station.hasSong();
    }
}
