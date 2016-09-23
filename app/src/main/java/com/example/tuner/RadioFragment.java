package com.example.tuner;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class RadioFragment extends Fragment {
    private final static String RADIO_ID_PARAM = "RADIO";

    private Activity context;
    private ListView stationList;
    private Radio radio;

    public RadioFragment() {
        Log.d("TNR", "Creating Fragment Parent");
    }

    public static RadioFragment newInstance(Radio radio) {
        RadioFragment fragment = new RadioFragment();
        Bundle args = new Bundle();
        args.putInt(RADIO_ID_PARAM, radio.getIndex());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);

        if (this.getArguments() != null) {
            int radioIndex = this.getArguments().getInt(RADIO_ID_PARAM);
            this.radio = RadioMaster.getInstance().getRadio(radioIndex);
            Log.d("TNR", "Creating radio fragment for " + this.radio.getName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tuner_main,
                container, false);

        this.stationList = (ListView) rootView.findViewById(R.id.station_list);
        this.stationList.setAdapter(new StationListAdapter(this.context, this));
        Log.d("TNR", "Recreating StationListAdapter");
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

    public Radio getRadio() {
        return this.radio;
    }

    public ListView getStationList()
    {
        return this.stationList;
    }
}
