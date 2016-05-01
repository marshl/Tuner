package com.example.tuner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Xml;

import com.example.tuner.RadioMaster.SOUND_TYPE;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

public class Station implements Serializable {
    private int stationIndex;
    public String name;
    public String dj;
    public String genre;
    public Bitmap iconData;
    public boolean fullTrack = false;
    public boolean loadAll = false;
    public HashMap<SOUND_TYPE, ArrayList<File>> miscFileMap = new HashMap<SOUND_TYPE, ArrayList<File>>();
    private Radio parentRadio;
    private File directory;
    private File iconFile;
    private ArrayList<Song> songList = new ArrayList<Song>();
    //private int songIndex = 0;

    public int getIndex()
    {
        return this.stationIndex;
    }

    public Song[] getSongs()
    {
        // Come back in 2017
        //Collections.sort( this.songList, (o1,o2) -> o1.name.compareTo(o2.name));

        ArrayList<Song> tempSongList = new ArrayList<Song>(this.songList);
        Collections.sort(tempSongList, new Comparator<Song>() {
            @Override
            public int compare(Song o1, Song o2) {
                return o1.name.compareTo(o2.name);
            }
        });

        Song[] result = new Song[this.songList.size()];
        result = tempSongList.toArray(result);
        return result;
    }

    public Station(Radio _parentRadio, int _stationIndex, String _dir) throws IOException, XmlPullParserException {
        this.stationIndex = _stationIndex;
        this.parentRadio = _parentRadio;
        this.directory = new File(this.parentRadio.directory.toString() + "/" + _dir);
        if (!this.directory.exists()) {
            throw new IOException("Station directory \"" + this.directory.toString() + "\" not found");
        }

        this.miscFileMap.put(SOUND_TYPE.GENERAL, new ArrayList<File>());
        this.miscFileMap.put(SOUND_TYPE.ID, new ArrayList<File>());
        this.miscFileMap.put(SOUND_TYPE.TIME, new ArrayList<File>());
        this.miscFileMap.put(SOUND_TYPE.WEATHER, new ArrayList<File>());

        this.miscFileMap.put(SOUND_TYPE.TO_ADVERT, new ArrayList<File>());
        this.miscFileMap.put(SOUND_TYPE.TO_WEATHER, new ArrayList<File>());
        this.miscFileMap.put(SOUND_TYPE.TO_NEWS, new ArrayList<File>());

        this.loadXml();

        Collections.shuffle(this.songList);
        for (Entry<SOUND_TYPE, ArrayList<File>> entry : this.miscFileMap.entrySet()) {
            Collections.shuffle(entry.getValue());
        }
    }

    private void loadXml() throws IOException, XmlPullParserException {
        File dataFile = new File(this.directory.toString() + "/station.xml");
        if (!dataFile.exists()) {
            throw new IOException("Could not find " + dataFile.toString());
        }

        FileInputStream fileStream = new FileInputStream(dataFile);

        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(fileStream, null);
        parser.nextTag();
        this.readStationData(parser);

        fileStream.close();
    }

    private void readStationData(XmlPullParser _parser) throws IOException, XmlPullParserException {
        _parser.require(XmlPullParser.START_TAG, null, "station");

        this.name = _parser.getAttributeValue(null, "name");
        this.dj = _parser.getAttributeValue(null, "dj");
        this.genre = _parser.getAttributeValue(null, "genre");

        String loadAllStr = _parser.getAttributeValue(null, "loadall");
        this.loadAll = loadAllStr != null && loadAllStr.equals("true");

        if (this.loadAll) {
            this.loadAllMusicFiles();
        }

        String iconName = _parser.getAttributeValue(null, "icon");
        if (iconName != null) {
            this.iconFile = new File(this.directory + "/" + iconName);
            File pngFile = new File(this.iconFile.toString().replaceFirst(".bmp", ".png"));
            if (pngFile.isFile()) {
                this.iconData = BitmapFactory.decodeFile(pngFile.toString());
            } else {
                this.iconData = BitmapFactory.decodeFile(iconFile.toString());
            }
        }

        String fullTrackStr = _parser.getAttributeValue(null, "fulltrack");
        this.fullTrack = fullTrackStr != null && fullTrackStr.equals("true");

        while (_parser.next() != XmlPullParser.END_TAG) {
            if (_parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = _parser.getName();

            if (name.equals("general")
                    || name.equals("weather")
                    || name.equals("id")
                    || name.equals("time")
                    || name.equals("to_ad")
                    || name.equals("to_news")
                    || name.equals("to_weather")) {
                String filename = _parser.getAttributeValue(null, "file");
                File file = new File(this.directory.toString() + "/" + filename);

                if (!RadioMaster.checkFile(file)) {
                    RadioMaster.skip(_parser);
                    continue;
                }

                if (name.equals("general")) {
                    this.miscFileMap.get(SOUND_TYPE.GENERAL).add(file);
                } else if (name.equals("weather")) {
                    this.miscFileMap.get(SOUND_TYPE.WEATHER).add(file);
                } else if (name.equals("id")) {
                    this.miscFileMap.get(SOUND_TYPE.ID).add(file);
                } else if (name.equals("time")) {
                    this.miscFileMap.get(SOUND_TYPE.TIME).add(file);
                } else if (name.equals("to_ad")) {
                    this.miscFileMap.get(SOUND_TYPE.TO_ADVERT).add(file);
                } else if (name.equals("to_news")) {
                    this.miscFileMap.get(SOUND_TYPE.TO_NEWS).add(file);
                } else if (name.equals("to_weather")) {
                    this.miscFileMap.get(SOUND_TYPE.TO_WEATHER).add(file);
                }
                RadioMaster.skip(_parser);
            } else if (name.equals("song")) {
                this.readSong(_parser);
            } else {
                RadioMaster.skip(_parser);
            }
        }
    }

    private void loadAllMusicFiles() {
        Log.d(this.name, "loading all music files");
        this.loadMusicFilesFromDir(this.directory);
    }

    private void loadMusicFilesFromDir(File _dir) {
        for (File file : _dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".mp3") || s.endsWith(".ogg");
            }
        })) {
            if (file.isDirectory()) {
                this.loadMusicFilesFromDir(file);
                continue;
            }

            Song song = new Song(this);
            // Remove file extension
            song.name = file.getName().replaceFirst("[.][^.]+$", "");
            song.artist = this.dj;
            song.main = file;
            this.songList.add(song);
        }
    }

    private void readSong(XmlPullParser _parser) throws XmlPullParserException, IOException {
        _parser.require(XmlPullParser.START_TAG, null, "song");

        Song song = new Song(this);

        song.name = _parser.getAttributeValue(null, "name");
        song.artist = _parser.getAttributeValue(null, "artist");

        while (_parser.next() != XmlPullParser.END_TAG) {
            if (_parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = _parser.getName();

            if (name.equals("in")
                    || name.equals("out")
                    || name.equals("main")) {
                String dir = _parser.getAttributeValue(null, "file");

                File file = new File(this.directory.toString() + "/" + dir);
                if (!RadioMaster.checkFile(file)) {
                    RadioMaster.skip(_parser);
                    continue;
                }

                if (name.equals("in")) {
                    song.introList.add(file);
                } else if (name.equals("out")) {
                    song.outroList.add(file);
                } else {
                    song.main = file;
                }
                RadioMaster.skip(_parser);
            } else {
                RadioMaster.skip(_parser);
            }
        }
        this.songList.add(song);
    }

    public Song getNextSong() {
        Song song = this.songList.get(0);
        this.songList.remove(0);
        this.songList.add(song);
        return song;
    }

    public File getNextMiscFile(SOUND_TYPE _soundType) {
        ArrayList<File> list = this.miscFileMap.get(_soundType);

        if (list.isEmpty()) {
            return null;
        }

        File file = list.get(0);
        list.remove(0);
        list.add(file);
        return file;
    }

    public boolean hasSong() {
        return !this.songList.isEmpty();
    }

    public int getSongCount() {
        return this.songList.size();
    }

    public Song getSongAtIndex(int _position) {
        return _position < this.songList.size() ? this.songList.get(_position) : null;
    }

    /*public void setSongIndex(int _songIndex) {
        this.songIndex = _songIndex;
    }*/

    public Radio getParentRadio() {
        return this.parentRadio;
    }
}
