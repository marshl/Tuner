package com.example.tuner;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class RadioFragment extends Fragment {
    private static String RADIO_ID_PARAM = "RADIO";

    public Activity context;
    public int radioIndex;
    public Radio radio;

    public ListView stationList;

    public RadioFragment() {

    }

    public static RadioFragment newInstance(int _radioIndex) {
        RadioFragment fragment = new RadioFragment();
        Bundle args = new Bundle();
        args.putInt(RADIO_ID_PARAM, _radioIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);

        if (this.getArguments() != null) {
            this.radioIndex = getArguments().getInt(RADIO_ID_PARAM);
            this.radio = RadioMaster.instance.getRadio(this.radioIndex);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tuner_main,
                container, false);

        this.stationList = (ListView) rootView.findViewById(R.id.station_list);
        this.stationList.setAdapter(new StationListAdapter(this.context, this));
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
        this.context = null;
    }
}
