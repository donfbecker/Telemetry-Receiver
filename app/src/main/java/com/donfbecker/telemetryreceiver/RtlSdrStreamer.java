package com.donfbecker.telemetryreceiver;

import java.io.DataInputStream;
import java.io.InputStream;
import java.lang.Thread;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class RtlSdrStreamer {
    // Sample rate must be between 225001 and 300000 or 900001 and 3200000
    public static final int IQ_SAMPLE_RATE = 240000;
    public static final int AUDIO_SAMPLE_RATE = 48000;
    public static final int SAMPLE_RATIO = 5;

    public static final byte COMMAND_SET_FREQUENCY        = 0x01;
    public static final byte COMMAND_SET_SAMPLERATE       = 0x02;
    public static final byte COMMAND_SET_GAIN_MODE        = 0x03;
    public static final byte COMMAND_SET_GAIN             = 0x04;
    public static final byte COMMAND_SET_FREQ_CORR        = 0x05;
    public static final byte COMMAND_SET_IFGAIN           = 0x06;
    public static final byte COMMAND_SET_AGC_MODE         = 0x08;
    public static final byte COMMAND_SET_DIRECT_SAMPLING  = 0x09;
    public static final byte COMMAND_SET_TUNING_OFFSET    = 0x0a;
    public static final byte COMMAND_SET_RTL_XTAL         = 0x0b;
    public static final byte COMMAND_SET_TUNER_XTAL       = 0x0c;
    public static final byte COMMAND_SET_TUNER_GAIN_BY_ID = 0x0d;
    public static final byte COMMAND_SET_BIAS_TEE         = 0x0e;

    private Socket connection;
    private InputStream stream;

    private boolean stayAlive = false;
    private AudioTrack audioTrack;
    private int trackBufferSize;
    private int iqBufferSize;

    private float softwareGain = 1;
    private float attenuation = 1;
    private float squelch = 0;
    private boolean filterEnabled = false;
    private boolean detectorEnabled = false;

    private LowPassFilter filter;
    private ToneDetector detector;

    private ArrayBlockingQueue<byte[]> commandQueue = null;

    public RtlSdrStreamer() {
        commandQueue = new ArrayBlockingQueue<byte[]>(100);
        filter = new LowPassFilter();
        detector = new ToneDetector(48000, 200, 900, 10);
    }

    public boolean isRunning() {
        return false;
    }

    public boolean start() {
        Log.d("DEBUG", "RtlSdrStreamer.start()");

        trackBufferSize = AudioTrack.getMinBufferSize(AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        iqBufferSize = trackBufferSize * SAMPLE_RATIO * 2;

        Log.d("DEBUG", "trackBufferSize = " + trackBufferSize);

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, trackBufferSize, AudioTrack.MODE_STREAM);

        new Thread() {
            short[] trackBuffer = new short[trackBufferSize];
            byte[] iqBuffer = new byte[iqBufferSize];

            public void run() {
                try {
                    connection = new Socket("127.0.0.1", 1234);
                    stream = connection.getInputStream();
                    DataInputStream input = new DataInputStream(stream);

                    // Let's try disabling AGC
                    setAGCMode(false);
                    setGainMode(true);

                    audioTrack.play();
                    stayAlive = true;
                    while (stayAlive) {
                        input.readFully(iqBuffer);

                        for (int j = 0; j < trackBufferSize; j++) {
                            double sumI = 0;
                            double sumQ = 0;
                            for (int k = 0; k < SAMPLE_RATIO; k++) {
                                sumI += (double) (((iqBuffer[(j * (SAMPLE_RATIO * 2)) + (k * 2)] & 0xFF) - 127.5) / 127.5);
                                sumQ += (double) (((iqBuffer[(j * (SAMPLE_RATIO * 2)) + (k * 2) + 1] & 0xFF) - 127.5) / 127.5);
                            }

                            double i = (sumI / SAMPLE_RATIO);
                            double q = (sumQ / SAMPLE_RATIO);
                            double a = ((i * i) + (q * q));
                            //float v = (float)(i * q);
                            float v = (float)((i + q) * (softwareGain / attenuation));
                            if(filterEnabled) v = filter.filter(v);
                            if(detectorEnabled) v = detector.filter(v);

                            if(a < squelch) v = 0f;
                            if(v > 1) v = 1;
                            if(v < -1) v = -1;

                            trackBuffer[j] = (short)(v * 32767);
                        }

                        audioTrack.write(trackBuffer, 0, trackBufferSize);

                        // Check command queue for packets
                        byte[] packet = commandQueue.poll();
                        if (packet != null) connection.getOutputStream().write(packet);
                    }

                    audioTrack.stop();
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        return true;
    }

    public boolean stop() {
        stayAlive = false;
        return true;
    }

    public boolean setFrequency(int frequency) {
        return sendCommand(COMMAND_SET_FREQUENCY, frequency);
    }

    public boolean setTunerOffset(int offset) {
        return sendCommand(COMMAND_SET_TUNING_OFFSET, offset);
    }

    public boolean setFrequencyCorrection(int correction) {
        return sendCommand(COMMAND_SET_FREQ_CORR, correction);
    }

    public boolean setGainMode(boolean manual) {
        return sendCommand(COMMAND_SET_GAIN_MODE, manual ? 0x01 : 0x00);
    }

    public boolean setGain(int gain) {
        return sendCommand(COMMAND_SET_GAIN, gain);
    }

    public boolean setSoftwareGain(float gain) {
        this.softwareGain = gain * 1.0f;
        return true;
    }

    public boolean setAttenuation(float attenuation) {
        this.attenuation = attenuation;
        return true;
    }

    public boolean setSquelch(int squelch) {
        this.squelch = (float)squelch / 10000.0f;
        Log.d("DEBUG", "Squelch is " + this.squelch);
        return true;
    }

    public boolean setFilterEnabled(boolean enabled) {
        this.filterEnabled = enabled;
        return true;
    }

    public boolean setDetectorEnabled(boolean enabled) {
        this.detectorEnabled = enabled;
        return true;
    }

    public boolean setAGCMode(boolean enabled) {
        return sendCommand(COMMAND_SET_AGC_MODE, enabled ? 0x01 : 0x00);
    }

    public boolean setBiasTee(boolean enabled) {
        return sendCommand(COMMAND_SET_BIAS_TEE, enabled ? 0x01 : 0x00);
    }

    private boolean sendCommand(byte command, int arg) {
        if(connection == null || !connection.isConnected()) return false;
        commandQueue.offer(commandToPacket(command, arg));
        return true;
    }

    private boolean sendCommand(byte command, short arg1, short arg2) {
        if(connection == null || !connection.isConnected()) return false;
        commandQueue.offer(commandToPacket(command, arg1, arg2));
        return true;
    }
    private byte[] commandToPacket(byte command, int arg) {
        byte[] packet = new byte[5];
        packet[0] = command;
        packet[1] = (byte)((arg >> 24) & 0xFF);
        packet[2] = (byte)((arg >> 16) & 0xFF);
        packet[3] = (byte)((arg >> 8) & 0xFF);
        packet[4] = (byte)(arg & 0xFF);

        return packet;
    }

    private byte[] commandToPacket(byte command, short arg1, short arg2) {
        byte[] packet = new byte[5];
        packet[0] = command;
        packet[1] = (byte)((arg1 >> 8) & 0xFF);
        packet[2] = (byte)(arg1 & 0xFF);
        packet[3] = (byte)((arg2 >> 8) & 0xFF);
        packet[4] = (byte)(arg2 & 0xFF);

        return packet;
    }
}
