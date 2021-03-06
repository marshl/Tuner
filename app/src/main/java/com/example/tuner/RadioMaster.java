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
    private static RadioMaster instance;
    private SOUND_TYPE lastPlayedSoundType = SOUND_TYPE.SONG;

    private final ArrayList<Radio> radioList = new ArrayList<Radio>();
    private Radio currentRadio;

    public RadioMaster() {
        RadioMaster.instance = this;
    }

    public static RadioMaster getInstance() {
        return RadioMaster.instance;
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
        if (_file.exists()) {
            return true;
        }

        Log.w("TNR", "File not found: " + _file.toString());
        return false;
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
    }

    public Radio getCurrentRadio() {
        return this.currentRadio;
    }

    public void setCurrentRadio(Radio radio) {
        this.currentRadio = radio;
    }

    public SoundFileList getNextFileBlock(SOUND_TYPE _soundType) throws IllegalArgumentException {
        SoundFileList fileList;

        final Radio currentRadio = this.getCurrentRadio();
        final Station currentStation = currentRadio.getCurrentStation();

        Random tempRand = new Random();
        boolean playMiscIntro = tempRand.nextFloat() > 0.5f;

        if (_soundType == SOUND_TYPE.SONG) {
            Song s = currentStation.getNextSong();
            if (s == null)
                return null;

            fileList = s.getFileList();

        } else {
            fileList = new SoundFileList(_soundType, null);

            switch (_soundType) {
                case ADVERT: {
                    if (playMiscIntro && currentStation.hasFileOfType(SOUND_TYPE.TO_ADVERT)) {
                        fileList.addFile(currentStation.getNextMiscFile(SOUND_TYPE.TO_ADVERT));
                    }

                    fileList.addFile(currentRadio.getNextAdvert());
                    break;
                }
                case GENERAL:
                case ID:
                case TIME: {
                    if (currentStation.hasFileOfType(_soundType)) {
                        fileList.addFile(currentStation.getNextMiscFile(_soundType));
                    }
                    break;
                }
                case WEATHER: {
                    if (playMiscIntro && currentStation.hasFileOfType(SOUND_TYPE.TO_WEATHER)) {
                        fileList.addFile(currentStation.getNextMiscFile(SOUND_TYPE.TO_WEATHER));
                    }

                    if (this.getCurrentRadio().hasWeather()) {
                        fileList.addFile(this.getCurrentRadio().getNextWeather());
                    } else if (currentStation.hasFileOfType(_soundType)) {
                        fileList.addFile(currentStation.getNextMiscFile(_soundType));
                    }

                    break;
                }
                case NEWS: {
                    if (playMiscIntro && currentStation.hasFileOfType(SOUND_TYPE.TO_NEWS)) {
                        fileList.addFile(currentStation.getNextMiscFile(SOUND_TYPE.TO_NEWS));
                    }

                    fileList.addFile(this.getCurrentRadio().getNextNews());

                    break;
                }
                default: {
                    throw new IllegalArgumentException("Uncaught SOUND_TYPE " + _soundType);
                }
            }
        }

        this.lastPlayedSoundType = _soundType;
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

    public static String soundTypeToLabel(RadioMaster.SOUND_TYPE _soundType) {
        switch (_soundType) {
            case GENERAL:
                return "General";
            case WEATHER:
                return "Weather";
            case ID:
                return "Station ID";
            case TIME:
                return "Time";
            case SONG:
                return "Song";
            case ADVERT:
                return "Advert";
            case NEWS:
                return "News";
            case TO_NEWS:
                return "News Transition";
            case TO_WEATHER:
                return "Weather Transition";
            case TO_ADVERT:
                return "Advert Transition";
            default:
                throw new IllegalArgumentException("Uncaught SOUND_TYPE" + _soundType);
        }
    }
}
