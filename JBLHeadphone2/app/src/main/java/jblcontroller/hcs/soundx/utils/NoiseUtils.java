package jblcontroller.hcs.soundx.utils;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import static jblcontroller.hcs.soundx.utils.AppConstants.NOISE_LEVEL_THRESHOLD;

public class NoiseUtils {
    private final String TAG = "NoiseUtils";
    private MediaRecorder mMediaRecorder;
    AudioRecord recorder;
    private Context mContext;
    public static final int MAX_LENGTH = 1000 * 60 * 10;// The maximum recording time 1000*60*10;
    private final Handler mHandler = new Handler();

    private NoiseTrackListener mNoiseListener;
    public static double REFERENCE = 0.00002;
    private long startTime;
    private long endTime;

    public NoiseUtils(Context ctx, NoiseTrackListener listener) {
        this.mContext = ctx;
        mNoiseListener = listener;
    }

    /**
     * To start recording in AMR format
     * <p>
     * The recording file
     */
//    public void startRecord1() {
//        // To start recording
//        /* ①Initial: Instantiate a MediaRecorder object */
//        if (mMediaRecorder == null)
//            mMediaRecorder = new MediaRecorder();
//        try {
//            File directory = mContext.getFilesDir();
//            File file = new File(directory, "AUDIO");
//            /* ②setAudioSource/setVedioSource */
//            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// A microphone
//            /*
//             * To set the output file format: THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP (3gp format
//             * , H263 video /ARM audio coding), MPEG-4, RAW_AMR (only supports audio and audio coding for AMR_NB)
//             */
//            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
//            /* ② set audio file encoding: AAC/AMR_NB/AMR_MB/Default sound (waveform) sampling */
//            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
//            /* Preparation */
//            mMediaRecorder.setOutputFile(file.getAbsolutePath());
//            mMediaRecorder.setMaxDuration(MAX_LENGTH);
//            mMediaRecorder.prepare();
//            /* The start */
//            mMediaRecorder.start();
//            // AudioRecord audioRecord.
//            /* Gets the start time* */
//            startTime = System.currentTimeMillis();
//            // pre=mMediaRecorder.getMaxAmplitude();
//           // updateMicStatus();
//            Log.i("ACTION_START", "startTime" + startTime);
//        } catch (IllegalStateException e) {
//            Log.i(TAG,
//                    "call startAmr(File mRecAudioFile) failed!"
//                            + e.getMessage());
//        } catch (IOException e) {
//            Log.i(TAG,
//                    "call startAmr(File mRecAudioFile) failed!"
//                            + e.getMessage());
//        }
//
//    }


    public void stopRecord() {
        mHandler.removeCallbacks(mUpdateMicStatusTimer);
        if (recorder != null) {
            recorder.stop();
            recorder.release();
        }
    }

//        if (mMediaRecorder == null)
//            return 0L;
//        endTime = System.currentTimeMillis();
//        Log.i("ACTION_END", "endTime" + endTime);
//        mMediaRecorder.stop();
//        mMediaRecorder.reset();
//        mMediaRecorder.release();
//        mMediaRecorder = null;
//        Log.i("ACTION_LENGTH", "Time" + (endTime - startTime));
//        return endTime - startTime;

    private Runnable mUpdateMicStatusTimer = new Runnable() {
        public void run() {
            try {
                getNoiseLevel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Update the microphone state DB is the relative loudness decibels formula K=20lg (Vo/Vi) Vo current amplitude value Vi benchmark value of 600: how do I make reference value as 20?
     * * Math.log10(mMediaRecorder.getMaxAmplitude() / Vi)==0 when VI is all that I need a reference value
     * When I'm not into the microphone say any words, testing for mMediaRecorder.getMaxAmplitude () value is the reference value.
     * Log.i("mic_", "Microphone reference value: "+ mMediaRecorder.getMaxAmplitude ()); the premise is not the microphone say anything
     */
    private int SPACE = 15000;// Sampling time

//    private void updateMicStatus() {
//        if (mMediaRecorder != null) {
//            // int vuSize = 10 * mMediaRecorder.getMaxAmplitude() / 32768;
//            int ratio = mMediaRecorder.getMaxAmplitude();
//            int db = 0;// DB
//            if (ratio > 1)
//                db = (int) (20 * Math.log10(ratio/ 2700.0));
//            Log.i("Decibel value:", "" + db);
//            if (db > NOISE_LEVEL_THRESHOLD)
//                mNoiseListener.onNoiseDetect();
//            else
//                mNoiseListener.onQuietEnvironment();
//        }
//        mHandler.postDelayed(mUpdateMicStatusTimer, SPACE);
//        /*
//         * if (db > 1) { vuSize = (int) (20 * Math.log10(db)); Log.i("mic_",
//         * "The microphone volume: "+ vuSize);} else Log.i (" mic_ "," the microphone volume size: " + 0);
//         */
//    }

    public void startRecord()
    {
        try {
            getNoiseLevel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void getNoiseLevel() throws Exception
    {
        //Log.e(TAG, "start new recording process");
        int bufferSize = AudioRecord.getMinBufferSize(44100,AudioFormat.CHANNEL_IN_DEFAULT,AudioFormat.ENCODING_PCM_16BIT);
        //making the buffer bigger....
        bufferSize=bufferSize*4;
        AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                44100, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        short data [] = new short[bufferSize];
        double average = 0.0;
        recorder.startRecording();
        //recording data;
        recorder.read(data, 0, bufferSize);
        recorder.stop();
        //Log.e(TAG, "stop");
        for (short s : data)
        {
            if(s>0)
                average += Math.abs(s);
            else
                bufferSize--;
        }
        //x=max;
        double x = average/bufferSize;
      //  Log.e(TAG, ""+x);
        recorder.release();
       // Log.d(TAG, "getNoiseLevel() ");
        double db=0;
        // calculating the pascal pressure based on the idea that the max amplitude (between 0 and 32767) is
        // relative to the pressure
        double pressure = x/51805.5336; //the value 51805.5336 can be derived from asuming that x=32767=0.6325 Pa and x=1 = 0.00002 Pa (the reference value)
      //  Log.d(TAG, "x="+pressure +" Pa");
        db = (20 * Math.log10(pressure/REFERENCE));
        Log.d(TAG, "db="+db);
        mHandler.postDelayed(mUpdateMicStatusTimer, SPACE);
        if (db > NOISE_LEVEL_THRESHOLD)
                mNoiseListener.onNoiseDetect();
            else
                mNoiseListener.onQuietEnvironment();
    }
}
