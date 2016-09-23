package com.example.tuner;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class SongListFragment extends DialogFragment {
    private static final String RADIO_ID_PARAM = "RADIO";
    private static final String STATION_ID_PARAM = "STATION";
    private Activity context;
    private Station station;

    public SongListFragment() {

    }

    public static SongListFragment newInstance(Station station) {
        SongListFragment fragment = new SongListFragment();
        Bundle args = new Bundle();
        args.putInt(RADIO_ID_PARAM, station.getParentRadio().getIndex());
        args.putInt(STATION_ID_PARAM, station.getIndex());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);

        if (this.getArguments() != null) {
            int radioIndex = getArguments().getInt(RADIO_ID_PARAM);
            int stationIndex = getArguments().getInt(STATION_ID_PARAM);
            this.station = RadioMaster.getInstance().getRadio(radioIndex).getStation(stationIndex);
        }
    }

    @Override
    public View onCreateView(LayoutInflater _inflater, ViewGroup _container, Bundle _savedInstanceState) {
        View rootView = _inflater.inflate(R.layout.fragment_song_list, _container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.song_list_view);
        listView.setAdapter(new SongListAdapter(this, this.context, this.station.getSongs()));
        getDialog().setTitle(this.station.getParentRadio().getName() + ": " + this.station.getName());
        return rootView;
    }

    @Override
    public void onAttach(Activity _activity) {
        super.onAttach(_activity);
        this.context = _activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
