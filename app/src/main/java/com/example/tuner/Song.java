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

    public File getNextIntro() {
        if (this.introList.isEmpty()) {
            return null;
        }

        File intro = this.introList.get(0);
        this.introList.remove(0);
        this.introList.add(intro);
        return intro;
    }

    public File getNextOutro() {
        if (this.outroList.isEmpty()) {
            return null;
        }

        File outro = this.outroList.get(0);
        this.outroList.remove(0);
        this.outroList.add(outro);
        return outro;
    }

    public SoundFileList getFileList() {
        SoundFileList fileList = new SoundFileList();

        fileList.song = this;
        fileList.mainFile = this.main;

        fileList.introFile = this.getNextIntro();
        fileList.outroFile = this.getNextOutro();

        return fileList;
    }

    public String getName() {
        return this.name;
    }

    public String getArtist() {
        return this.artist;
    }

    public File getMainFIle() {
        return this.main;
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
