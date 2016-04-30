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
    private OnFragmentInteractionListener interactionListener;

    private int radioIndex;
    private int stationIndex;
    private Station station;

    public SongListFragment() {

    }

    public static SongListFragment newInstance(int _radioIndex, int _stationIndex) {
        SongListFragment fragment = new SongListFragment();
        Bundle args = new Bundle();
        args.putInt(RADIO_ID_PARAM, _radioIndex);
        args.putInt(STATION_ID_PARAM, _stationIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);

        if (this.getArguments() != null) {
            this.radioIndex = getArguments().getInt(RADIO_ID_PARAM);
            this.stationIndex = getArguments().getInt(STATION_ID_PARAM);
            this.station = RadioMaster.instance.getRadio(this.radioIndex).getStation(this.stationIndex);
        }
    }

    @Override
    public View onCreateView(LayoutInflater _inflater, ViewGroup _container, Bundle _savedInstanceState) {
        View rootView = _inflater.inflate(R.layout.fragment_song_list, _container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.song_list_view);
        listView.setAdapter(new SongListAdapter(this, this.station));
        getDialog().setTitle(RadioMaster.instance.getRadio(this.radioIndex).name + ": " + this.station.name);
        return rootView;
    }

    @Override
    public void onAttach(Activity _activity) {
        super.onAttach(_activity);
        this.context = _activity;
        try {
            this.interactionListener = (OnFragmentInteractionListener) _activity;
        } catch (ClassCastException _e) {
            throw new ClassCastException(_activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.interactionListener = null;
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
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(int _songIndex);
    }

}
