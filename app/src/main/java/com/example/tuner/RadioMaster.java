package com.example.tuner;

import android.os.Environment;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class RadioMaster {
    public static RadioMaster instance;
    private SOUND_TYPE lastPlayedSoundType = SOUND_TYPE.SONG;

    private final ArrayList<Radio> radioList = new ArrayList<Radio>();
    private Radio currentRadio;

    public RadioMaster() {
        instance = this;
    }

    public static void skip(XmlPullParser _parser) throws XmlPullParserException, IOException {
        if (_parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }

        int depth = 1;

        while (depth != 0) {
            switch (_parser.next()) {
                case XmlPullParser.START_TAG:
                    ++depth;
                    break;
                case XmlPullParser.END_TAG:
                    --depth;
                    break;
            }
        }
    }

    public static boolean checkFile(File _file) {
        if (!_file.exists()) {
            Log.w("TNR", "File not found: " + _file.toString());
        }

        return _file.exists();
    }

    public void loadRadioDefinitions(String[] radioNames) throws IOException, XmlPullParserException {
        File rootFile = Environment.getExternalStorageDirectory();
        File musicFolder = new File(rootFile.toString() + "/Music");
        if (!musicFolder.exists()) {
            throw new IOException("Cannot find music directory");
        }

        for (String str : radioNames) {
            File radioDir = new File(musicFolder.toString() + "/" + str);
            if (radioDir.exists()) {
                Radio radio = new Radio(this, this.radioList.size(), radioDir);
                this.radioList.add(radio);
            } else {
                Log.w("TNR", "Radio folder " + radioDir.toString() + " does not exist");
            }
        }

        this.currentRadio = this.radioList.get(0);
    }

    public Radio getCurrentRadio() {
        return this.currentRadio;
    }

    public void setCurrentRadio(Radio radio) {
        this.currentRadio = radio;
    }

    public SoundFileList getNextFileBlock(SOUND_TYPE _soundType) throws IllegalArgumentException {
        SoundFileList fileList = new SoundFileList();

        final Radio currentRadio = this.getCurrentRadio();
        final Station currentStation = currentRadio.getCurrentStation();

        Random tempRand = new Random();
        boolean playMiscIntro = tempRand.nextFloat() > 0.5f;
        boolean playSongIntro = tempRand.nextFloat() > 0.5f;
        boolean playSongOutro = tempRand.nextFloat() > 0.5f;

        switch (_soundType) {
            case SONG: {
                Song s = currentStation.getNextSong();
                if (s == null)
                    return null;

                fileList = s.getFileList();
                fileList.usesOverlay = this.getCurrentRadio().hasSongOverlays();

                if (fileList.usesOverlay) {
                    if (!playSongIntro) {
                        fileList.introFile = null;
                    }
                    if (!playSongOutro) {
                        fileList.outroFile = null;
                    }
                }
                break;
            }
            case ADVERT: {
                fileList.mainFile = currentRadio.getNextAdvert();
                if (playMiscIntro) {
                    fileList.introFile = currentStation.getNextMiscFile(SOUND_TYPE.TO_ADVERT);
                }
                break;
            }
            case GENERAL:
            case ID:
            case TIME: {
                fileList.mainFile = this.getCurrentRadio().getCurrentStation().getNextMiscFile(_soundType);
                break;
            }
            case WEATHER: {
                if (this.getCurrentRadio().hasWeather()) {
                    fileList.mainFile = this.getCurrentRadio().getNextWeather();
                } else {
                    fileList.mainFile = this.getCurrentRadio().getCurrentStation().getNextMiscFile(_soundType);
                }

                if (playMiscIntro) {
                    fileList.introFile = currentStation.getNextMiscFile(SOUND_TYPE.TO_WEATHER);
                }
                break;
            }
            case NEWS: {
                fileList.mainFile = this.getCurrentRadio().getNextNews();
                if (playMiscIntro) {
                    fileList.introFile = currentStation.getNextMiscFile(SOUND_TYPE.TO_NEWS);
                }
                break;
            }
            default: {
                throw new IllegalArgumentException("Uncaught SOUND_TYPE " + _soundType);
            }
        }

        this.lastPlayedSoundType = _soundType;
        fileList.type = _soundType;
        return fileList;
    }

    public SOUND_TYPE getRandomSoundType(boolean _reset) {
        if (_reset) {// On a reset, play a station ID/general first (if applicable)
            if (this.getCurrentRadio().canPlaySoundType(SOUND_TYPE.ID)) {
                return SOUND_TYPE.ID;
            } else if (this.getCurrentRadio().canPlaySoundType(SOUND_TYPE.GENERAL)) {
                return SOUND_TYPE.GENERAL;
            }
        }


        // Build a list of weights, with a more songs than the rest to give it a higher chance
        ArrayList<SOUND_TYPE> typeWeights = new ArrayList<SOUND_TYPE>();
        for (int i = 0; i < 8; ++i) {
            typeWeights.add(SOUND_TYPE.SONG);
        }
        typeWeights.add(SOUND_TYPE.ADVERT);
        typeWeights.add(SOUND_TYPE.GENERAL);
        typeWeights.add(SOUND_TYPE.ID);
        typeWeights.add(SOUND_TYPE.NEWS);
        typeWeights.add(SOUND_TYPE.TIME);
        typeWeights.add(SOUND_TYPE.WEATHER);

        Collections.shuffle(typeWeights);

        // Check each type weight in a random order, until a valid one is found
        while (!typeWeights.isEmpty()) {
            SOUND_TYPE soundType = typeWeights.get(0);

            if (soundType != this.lastPlayedSoundType
                    && this.getCurrentRadio().canPlaySoundType(soundType)) {
                return soundType;
            } else {
                typeWeights.remove(0);
            }
        }

        // If there are no applicable files, use the last played type
        return this.lastPlayedSoundType;
    }

    public int getRadioCount() {
        return this.radioList.size();
    }

    public Radio getRadio(int _radioIndex) {
        return this.radioList.get(_radioIndex);
    }

    public enum SOUND_TYPE {
        GENERAL,
        WEATHER,
        ID,
        TIME,
        SONG,
        ADVERT,
        NEWS,

        TO_NEWS,
        TO_WEATHER,
        TO_ADVERT,
    }
}
