package com.example.tuner;

import android.app.Activity;
import android.app.FragmentManager;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class StationListAdapter implements ListAdapter {
    private Activity context;
    private RadioFragment fragmentParent;

    public StationListAdapter(Activity _context, RadioFragment _fragmentParent) {
        this.context = _context;
        this.fragmentParent = _fragmentParent;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getCount() {
        return this.fragmentParent.getRadio().getStationCount();
    }

    @Override
    public Object getItem(int _position) {
        return this.fragmentParent.getRadio().getStation(_position);
    }

    @Override
    public long getItemId(int _position) {
        return _position;
    }

    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public View getView(int _position, View _convertView, ViewGroup _parent) {
        if (_convertView == null) {
            LayoutInflater layoutInflater = this.fragmentParent.getActivity().getLayoutInflater();
            _convertView = layoutInflater.inflate(R.layout.station_list_item, _parent, false);
        }

        final Station station = this.fragmentParent.getRadio().getStation(_position);

        boolean selected = this.fragmentParent.getRadio().isSelected() && this.fragmentParent.getRadio().getCurrentStation() == station;

        LinearLayout stationRoot = (LinearLayout) _convertView.findViewById(R.id.station_list_root);
        stationRoot.setBackgroundColor(selected ? 0xff666666 : 0x00000000);

        final TextView nameTextView = (TextView) _convertView.findViewById(R.id.station_list_station_name);
        nameTextView.setText(station.name);

        final TextView djTextView = (TextView) _convertView.findViewById(R.id.station_list_station_dj);
        djTextView.setText(station.dj);

        final TextView genreTextView = (TextView) _convertView.findViewById(R.id.station_list_station_genre);
        genreTextView.setText(station.genre);

        final ImageView iconView = (ImageView) _convertView.findViewById(R.id.station_list_station_icon);
        iconView.setImageBitmap(station.iconData);

        _convertView.setOnClickListener(new StationListOnClickListener(station, this.fragmentParent.stationList));

        final int stationIndex = _position;

        _convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View _view) {
                FragmentManager fm = context.getFragmentManager();
                SongListFragment songListFragment = SongListFragment.newInstance(station);
                songListFragment.show(fm, "Pick a song");
                return true;
            }
        });

        return _convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.fragmentParent.getRadio().getStationCount() == 0;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    public static class StationListOnClickListener implements OnClickListener {

        public Radio radio;
        public Station station;

        public ListView listView;

        public StationListOnClickListener(Station station, ListView _listView) {

            this.station = station;
            this.radio = this.station.getParentRadio();
            this.listView = _listView;
        }

        @Override
        public void onClick(View v) {

            if (this.radio.getParentMaster().getCurrentRadio() != this.station.getParentRadio()
                    || this.station.getParentRadio().getCurrentStation() != this.station) {
                this.radio.getParentMaster().setCurrentRadio(this.radio);
                this.radio.setCurrentStation(this.station);
                Log.i("TNR", "???");
                try {
                    TunerAudioControl.instance.isResetting = true;
                    TunerAudioControl.instance.playNextItem();
                } catch (Exception _e) {
                    CustomLog.appendException(_e);
                    _e.printStackTrace();
                    throw new RuntimeException(_e);
                }
            }

            this.listView.invalidate();
        }
    }


}
