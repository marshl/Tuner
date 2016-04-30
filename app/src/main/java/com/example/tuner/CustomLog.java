package com.example.tuner;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

public class CustomLog {
    public static File getFile() {
        File logFile = new File(Environment.getExternalStorageDirectory() + "/Tuner.log");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException _e) {
                _e.printStackTrace();
            }
        }
        return logFile;
    }

    public static void appendException(Exception _e) {
        StringBuilder out = new StringBuilder();
        StackTraceElement[] trace = _e.getStackTrace();
        out.append(_e.toString());
        try {
            PrintStream stream = new PrintStream(getFile());
            _e.printStackTrace(stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        /*for ( StackTraceElement element : trace )
        {
            out.append( element.toString() ).append( '\n' );
        }

        appendString(out.toString());*/
    }

    public static void appendString(String _text) {
        File logFile = getFile();

        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(_text);
            buf.newLine();
            buf.close();
        } catch (IOException _e) {
            _e.printStackTrace();
        }
    }
}
