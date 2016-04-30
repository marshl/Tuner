package com.example.tuner;

import java.io.File;
import java.util.ArrayList;

public class Song {
    public String name;
    public String artist;
    public File main;

    public ArrayList<File> introList = new ArrayList<File>();
    public ArrayList<File> outroList = new ArrayList<File>();

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
}
