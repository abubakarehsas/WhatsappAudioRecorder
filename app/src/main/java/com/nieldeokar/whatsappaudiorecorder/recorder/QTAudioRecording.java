package com.nieldeokar.whatsappaudiorecorder.recorder;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class QTAudioRecording {

    private static final String TAG = "QTAudioRecording";
    private File file;
    private OnAudioRecordListener onAudioRecordListener;
    private long mStartingTimeMillis = 0;
    private static final int IO_ERROR = 1;
    private static final int RECORDER_ERROR = 2;

    private Thread mRecordingThread;

    QTAudioRecording() {
    }

    public void setOnAudioRecordListener(OnAudioRecordListener onAudioRecordListener) {
        this.onAudioRecordListener = onAudioRecordListener;
    }

    public void setFile(String filePath) {
        this.file = new File(filePath);
    }

    public synchronized void startRecording() {

        mStartingTimeMillis = System.currentTimeMillis();
        try {
            if(mRecordingThread != null) stopRecording(true);


            mRecordingThread = new Thread(new AudioRecordThread(outputStream(file),new AudioRecordThread.OnRecorderFailedListener() {
                @Override
                public void onRecorderFailed() {
                    onAudioRecordListener.onError(RECORDER_ERROR);
                    stopRecording(true);
                }

                @Override
                public void onRecorderStarted() {
                    onAudioRecordListener.onRecordingStarted();
                }
            }));
            mRecordingThread.setName("AudioRecordingThread");



            mRecordingThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public synchronized void stopRecording(Boolean cancel){

        Log.d(TAG, "Recording stopped ");

        if(mRecordingThread != null){

            mRecordingThread.interrupt();
            mRecordingThread = null;


            if (file.length() == 0L) {
                onAudioRecordListener.onError(IO_ERROR);
                return;
            }

            long mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);

            RecordingItem recordingItem = new RecordingItem();
            recordingItem.setFilePath(file.getAbsolutePath());
            recordingItem.setName(file.getName());
            recordingItem.setLength((int) mElapsedMillis);
            recordingItem.setTime(System.currentTimeMillis());

            if (!cancel) {
                onAudioRecordListener.onRecordFinished(recordingItem);
            } else {
                deleteFile();
            }
        }
    }

    private void deleteFile() {
        if (file != null && file.exists())
            Log.d(TAG, String.format("deleting file success %b ", file.delete()));
    }

    private OutputStream outputStream(File file) {
        if (file == null) {
            throw new RuntimeException("file is null !");
        }
        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(
                    "could not build OutputStream from" + " this file " + file.getName(), e);
        }
        return outputStream;
    }
}
