package com.example.tuner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SoundFileList {
    private final Song song; // Not set for non-song files
    private final List<File> fileList;
    private final RadioMaster.SOUND_TYPE type;

    private int fileIndex = 0;

    public SoundFileList(RadioMaster.SOUND_TYPE soundType, Song song) {
        this.type = soundType;
        this.song = song;

        this.fileList = new ArrayList<File>();
    }

    public RadioMaster.SOUND_TYPE getSoundType() {
        return this.type;
    }

    public Song getSong() {
        return this.song;
    }

    public void addFile(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }

        this.fileList.add(file);
    }

    public File getNextFile() {
        //assert (this.fileIndex < this.fileList.size());
        File file = this.fileList.get(this.fileIndex);
        this.fileIndex += 1;
        return file;
    }

    public File getFileAtIndex(int index) {
        assert (index >= 0 && index < this.fileList.size());
        return this.fileList.get(index);
    }

    public int getFileCount() {
        return this.fileList.size();
    }
}
