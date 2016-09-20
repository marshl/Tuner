package com.example.tuner;

import java.io.File;
import java.util.ArrayList;

public class Song {
    private final String name;
    private final String artist;
    private File main;

    private final ArrayList<File> introList = new ArrayList<File>();
    private final ArrayList<File> outroList = new ArrayList<File>();

    private final Station parentStation;

    public Song(Station station, String name, String artist) {
        this.parentStation = station;
        this.name = name;
        this.artist = artist;
    }

    public Station getParentStation() {
        return this.parentStation;
    }

    private File getNextIntro() {
        if (this.introList.isEmpty()) {
            return null;
        }

        File intro = this.introList.get(0);
        this.introList.remove(0);
        this.introList.add(intro);
        return intro;
    }

    private File getNextOutro() {
        if (this.outroList.isEmpty()) {
            return null;
        }

        File outro = this.outroList.get(0);
        this.outroList.remove(0);
        this.outroList.add(outro);
        return outro;
    }

    public SoundFileList getFileList() {
        SoundFileList fileList = new SoundFileList(RadioMaster.SOUND_TYPE.SONG, this);

        File intro = this.getNextIntro();
        File outro = this.getNextOutro();

        if (intro != null) {
            fileList.addFile(intro);
        }

        fileList.addFile(this.main);

        if (outro != null) {
            fileList.addFile(outro);
        }

        return fileList;
    }

    public String getName() {
        return this.name;
    }

    public String getArtist() {
        return this.artist;
    }

    public void addIntroFile(File file) {
        this.introList.add(file);
    }

    public void addOutroFile(File file) {
        this.outroList.add(file);
    }

    public void setMainFile(File file) {
        this.main = file;
    }
}
