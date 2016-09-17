package com.example.tuner;

import android.app.Activity;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

public class SongListAdapter implements ListAdapter {
    private final SongListFragment fragmentParent;
    private final Activity context;

    private final Song[] songs;

    public SongListAdapter(SongListFragment _fragmentParent, Activity context, Song[] songs) {
        this.fragmentParent = _fragmentParent;
        this.context = context;
        this.songs = songs;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int _i) {
        return false;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public int getCount() {
        return this.songs.length;
    }

    @Override
    public Object getItem(int index) {
        return this.songs[index];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = this.context.getLayoutInflater();
            convertView = layoutInflater.inflate(R.layout.song_list_item, parent, false);
        }

        final Song song = this.songs[position];

        final TextView nameTextView = (TextView) convertView.findViewById(R.id.song_item_song_name);
        nameTextView.setText(song.getName());

        final TextView artistTextView = (TextView) convertView.findViewById(R.id.song_item_song_artist);
        artistTextView.setText(song.getArtist());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TunerMain main = (TunerMain) context;
                main.selectSong(song);
                fragmentParent.dismiss();
            }
        });

        return convertView;
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
    public boolean isEmpty() {
        return this.songs.length == 0;
    }
}
