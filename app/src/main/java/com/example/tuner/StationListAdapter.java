package com.example.tuner;

import android.app.Activity;
import android.app.FragmentManager;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class StationListAdapter implements ListAdapter
{
    private Activity context;
	private RadioFragment fragmentParent;

    public StationListAdapter( Activity _context, RadioFragment _fragmentParent )
    {
        this.context = _context;
        this.fragmentParent = _fragmentParent;
    }

	@Override
	public void registerDataSetObserver( DataSetObserver observer )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unregisterDataSetObserver( DataSetObserver observer )
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getCount()
	{
		return this.fragmentParent.radio.stationList.size();
	}

	@Override
	public Object getItem( int _position )
	{
		return this.fragmentParent.radio.stationList.get( _position );
	}

	@Override
	public long getItemId( int _position )
	{
		return _position;
	}

	@Override
	public boolean hasStableIds()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public View getView( int _position, View _convertView, ViewGroup _parent )
	{
		if ( _convertView == null )
		{
			LayoutInflater layoutInflater = this.fragmentParent.getActivity().getLayoutInflater();
			_convertView = layoutInflater.inflate( R.layout.station_list_item, _parent, false );
		}

		final Station station = this.fragmentParent.radio.stationList.get( _position );
		
		boolean selected = RadioMaster.instance.getCurrentRadio().getCurrentStation() == station;
		
		LinearLayout stationRoot = (LinearLayout)_convertView.findViewById( R.id.station_list_root );
		stationRoot.setBackgroundColor( selected ? 0xff666666 : 0x00000000 );
		
		final TextView nameTextView = (TextView)_convertView.findViewById( R.id.station_list_station_name );
		nameTextView.setText( station.name );
		
		final TextView djTextView = (TextView)_convertView.findViewById( R.id.station_list_station_dj );
		djTextView.setText( station.dj );
		
		final TextView genreTextView = (TextView)_convertView.findViewById( R.id.station_list_station_genre );
		genreTextView.setText( station.genre );
		
		final ImageView iconView = (ImageView)_convertView.findViewById( R.id.station_list_station_icon );
		iconView.setImageBitmap( station.iconData );
		
		_convertView.setOnClickListener( new StationListOnClickListener( this.fragmentParent.radioIndex, 
				_position, this.fragmentParent.stationList ) );

        final int stationIndex = _position;
        /*_convertView.setOnLongClickListener( new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick( View _view )
            {
                FragmentManager fm = context.getFragmentManager();
                SongListFragment songListFragment = SongListFragment.newInstance( RadioMaster.instance.currentRadioIndex, stationIndex );
                return true;
            }
        });*/

		return _convertView;
	}

	@Override
	public int getItemViewType( int position )
	{
		return 0;
	}

	@Override
	public int getViewTypeCount()
	{
		return 1;
	}

	@Override
	public boolean isEmpty()
	{
		return this.fragmentParent.radio.stationList.isEmpty();
	}

	@Override
	public boolean areAllItemsEnabled()
	{
		return false;
	}

	@Override
	public boolean isEnabled( int position )
	{
		return false;
	}
	
	public static class StationListOnClickListener implements OnClickListener
	{
		public int radioIndex;
		public int stationIndex;

		public StationListOnClickListener( int _radioIndex, int _stationIndex, ListView _listView  )
		{
			this.radioIndex = _radioIndex;
			this.stationIndex = _stationIndex;
		}
		
		@Override
		public void onClick( View v )
		{
			// Do not make any changes if the current playing station was selected
			if ( RadioMaster.instance.currentRadioIndex != this.radioIndex
			  || RadioMaster.instance.getCurrentRadio().currentStationIndex != this.stationIndex )
			{
				RadioMaster.instance.currentRadioIndex = this.radioIndex;
				RadioMaster.instance.getCurrentRadio().currentStationIndex = this.stationIndex;
				
				try
				{
					TunerAudioControl.instance.isResetting = true;
					TunerAudioControl.instance.playNextItem();
				}
				catch ( Exception e )
				{
					e.printStackTrace();
				}
			}
		}
	}



}
