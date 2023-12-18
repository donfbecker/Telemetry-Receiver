package com.donfbecker.telemetryreceiver;

import com.donfbecker.rtlsdr.RtlCallback;
import com.donfbecker.rtlsdr.RtlDevice;
import com.donfbecker.rtlsdr.UsbStrings;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.net.Socket;

import android.content.Context;
import android.media.AudioTrack;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.util.Log;

// I think I should sample at 240kHz.  That is a multiple of 5 to reduce
// down to the audio sample rate of 48kHz.  If we read 320 samples each
// time, that reduces down to 64 audio samples, and represents about
// 1.3ms of time.  8 of those sample blocks would represent 10.4ms of
// pulse data, can can be combined to 512 samples to run though FFT.

public class RtlSdrStreamer implements RtlCallback {
    // Sample rate must be between 225001 and 300000 or 900001 and 3200000
    public static final int IQ_SAMPLE_RATE = 1536000;
    public static final int AUDIO_SAMPLE_RATE = 48000;
    public static final int SAMPLE_RATIO = 32;

    public static final double SQRT_TWO = Math.sqrt(2.0);

    public static final int MESSAGE_SIGNAL_STRENGTH       = 10001;

    public static final int[] GAIN_VALUES = {0, 9, 14, 27, 37, 77, 87, 125, 144, 157, 166, 197, 207, 229, 254, 280, 297, 328, 338, 364, 372, 386, 402, 421, 434, 439, 445, 480, 496};

    private RtlDevice device;

    private Socket connection;
    private InputStream stream;

    private boolean stayAlive = true;
    private AudioTrack audioTrack;
    private int trackBufferSize;
    private int iqBufferSize;

    private double softwareGain = 1.0d;
    private double attenuation = 1.0d;
    private double squelch = 0.0d;
    private boolean agcEnabled = false;

    private RCFilter iFilter;
    private RCFilter qFilter;

    private double d_table[];

    private int blocks = 0;
    private double blockPowerMax;

    public RtlSdrStreamer(Context ctx) {
        iFilter = new RCFilter(RCFilter.FILTER_LOWPASS, 24000.0d, 1.0d/IQ_SAMPLE_RATE);
        qFilter = new RCFilter(RCFilter.FILTER_LOWPASS, 24000.0d, 1.0d/IQ_SAMPLE_RATE);

        // Initialize byte to double table
        d_table = new double[256];
        for(int i = 0; i < 256; i++) {
            d_table[i] = (double)(((double)i - 127.5f) / 127.5f);
        }

        trackBufferSize = 64; //AudioTrack.getMinBufferSize(AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        iqBufferSize = trackBufferSize * SAMPLE_RATIO * 2;

        Log.d("RtlSdrStreamer", "trackBufferSize = " + trackBufferSize);
        Log.d("RtlSdrStreamer", "iqBufferSize = " + iqBufferSize);

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, trackBufferSize, AudioTrack.MODE_STREAM);

        RtlDevice.initialize(ctx);
        device = new RtlDevice(0);
    }

    public void rtlData(ByteBuffer buffer, int len) {
        short[] trackBuffer = new short[trackBufferSize];
        double[] iAvgBuffer = new double[trackBufferSize];
        double[] qAvgBuffer = new double[trackBufferSize];
        byte[] iqBuffer = new byte[iqBufferSize];

        double sumI;
        double sumQ;
        double sumA = 0;
        double i;
        double q;
        double a;
        double v;

        buffer.get(iqBuffer);
        for (int j = 0; j < trackBufferSize; j++) {
            sumI = 0;
            sumQ = 0;
            for (int k = 0; k < SAMPLE_RATIO; k++) {
                int offset = (j * (SAMPLE_RATIO * 2)) + (k * 2);

                // & 0xFF converts these to unsigned
                sumI += iFilter.filter(d_table[iqBuffer[offset] & 0xFF]);
                sumQ += qFilter.filter(d_table[iqBuffer[offset + 1] & 0xFF]);

            }

            // Attenuation should be run before filtering
            i = iAvgBuffer[j] = (sumI / SAMPLE_RATIO) * softwareGain * attenuation;
            q = qAvgBuffer[j] = (sumQ / SAMPLE_RATIO) * softwareGain * attenuation;

            // Divide by the square root of two to normalize max amplitude to 1.0
            a = Math.sqrt((i * i) + (q * q)) / SQRT_TWO;
            sumA += a;

            v = q; // q is the real component of the signal.

            //if(a < squelch) v = 0.0d;

            if(v > 1) v = 1;
            if(v < -1) v = -1;

            trackBuffer[j] = (short)(v * 32767);
        }

        double power = sumA / trackBufferSize;
        if(power > blockPowerMax) blockPowerMax = power;
        if(++blocks >= 20) {
            MainActivity.handler.sendMessage(MainActivity.handler.obtainMessage(MESSAGE_SIGNAL_STRENGTH, (int)(blockPowerMax * 1000000), 0));
            blocks = 0;
            blockPowerMax = 0.0;
        }

        int r = audioTrack.write(trackBuffer, 0, trackBufferSize);
    }

    public boolean start() {
        Log.d("RtlSdrStreamer", "RtlSdrStreamer.start()");
        if(RtlDevice.getDeviceCount() < 1) return false;

        RtlCallback cb = this;

        new Thread() {
            @Override
            public void run() {
                try {
                    audioTrack.play();

                    device.open();
                    //device.setAgcMode(0);
                    //device.setTunerGainMode(1);
                    device.resetBuffer();
                    device.readAsync(cb, 0, iqBufferSize);

                    audioTrack.stop();
                } catch (Exception e) {
                    Log.d("RtlSdrStreamer", e.getMessage());
                }
            }
        }.start();

        return true;
    }

    public boolean stop() {
        Log.d("DEBUG", "Stop requested");
        device.cancelAsync();
        device.close();
        return true;
    }

    public boolean setFrequency(long frequency) {
        device.setCenterFreq(frequency);
        return true;
    }

    public boolean setTunerOffset(int offset) {
        return true;
    }

    public boolean setFrequencyCorrection(int correction) {
        return true;
    }

    public boolean setGainMode(boolean manual) {
        device.setTunerGainMode(manual ? 1 : 0);
        return true;
    }

    public boolean setGain(int gain) {
        device.setTunerGain(gain);
        return true;
    }

    public boolean setSoftwareGain(double gain) {
        this.softwareGain = gain;
        return true;
    }

    public boolean setAttenuation(int attenuation) {
        // Attenuation should be provided between 0 and 100
        if(attenuation < 0 || attenuation > 100) return false;

        this.attenuation = 1.0 - (attenuation / 100.0);
        Log.d("DEBUG", "attenuation=" + this.attenuation);
        return true;
    }

    public boolean setSquelch(int squelch) {
        this.squelch = (double)squelch / 10000.0d;
        Log.d("DEBUG", "Squelch is " + this.squelch);
        return true;
    }

    public boolean setAGCMode(boolean enabled) {
        device.setAgcMode(enabled ? 1 : 0);
        return true;
    }

    public boolean setBiasTee(boolean enabled) {
        return true;
    }
}
