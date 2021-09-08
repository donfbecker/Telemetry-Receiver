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
    public static final int IQ_SAMPLE_RATE = 1920000;
    public static final int AUDIO_SAMPLE_RATE = 48000;
    public static final int SAMPLE_RATIO = 40;

    public static final byte COMMAND_SET_FREQUENCY 	= 0x01;
    public static final byte COMMAND_SET_SAMPLERATE = 0x02;
    public static final byte COMMAND_SET_GAIN_MODE	= 0x03;
    public static final byte COMMAND_SET_GAIN 		= 0x04;
    public static final byte COMMAND_SET_FREQ_CORR 	= 0x05;
    public static final byte COMMAND_SET_IFGAIN 	= 0x06;
    public static final byte COMMAND_SET_AGC_MODE 	= 0x08;

    private Socket connection;
    private InputStream stream;

    private boolean stayAlive = false;
    private AudioTrack audioTrack;
    private int trackBufferSize;
    private int iqBufferSize;

    private float softwareGain = 1;
    private float squelch = 0;

    private ArrayBlockingQueue<byte[]> commandQueue = null;

    public RtlSdrStreamer() {
        commandQueue = new ArrayBlockingQueue<byte[]>(100);
    }

    public boolean isRunning() {
        return false;
    }

    public boolean start() {
        Log.d("DEBUG", "RtlSdrStreamer.start()");

        trackBufferSize = AudioTrack.getMinBufferSize(AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT);
        iqBufferSize = trackBufferSize * SAMPLE_RATIO * 2;

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT, trackBufferSize, AudioTrack.MODE_STREAM);

        new Thread() {
            byte[] trackBuffer = new byte[trackBufferSize];
            byte[] iqBuffer = new byte[iqBufferSize];

            public void run() {
                try {
                    connection = new Socket("127.0.0.1", 1234);
                    stream = connection.getInputStream();
                    DataInputStream input = new DataInputStream(stream);

                    // Let's try disabling AGC
                    setAGCMode(false);
                    setGainMode(false);

                    audioTrack.play();
                    stayAlive = true;
                    while (stayAlive) {
                        input.readFully(iqBuffer);

                        for (int j = 0; j < trackBufferSize; j++) {
                            double sumI = 0;
                            double sumQ = 0;
                            for (int k = 0; k < SAMPLE_RATIO; k++) {
                                sumI += (double) (((iqBuffer[(j * (SAMPLE_RATIO * 2)) + (k * 2)] & 0xFF) - 128.0) / 128.0);
                                sumQ += (double) (((iqBuffer[(j * (SAMPLE_RATIO * 2)) + (k * 2) + 1] & 0xFF) - 128.0) / 128.0);
                            }

                            double i = (sumI / SAMPLE_RATIO);
                            double q = (sumQ / SAMPLE_RATIO);
                            double a = (i * i) + (q * q);
                            double v = (128.0 + ((i + q) * 128.0));
                            if(a < squelch) v = 128.0;

                            v = 128 + ((v - 128) * softwareGain);
                            trackBuffer[j] = (byte)v;
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

    public boolean setGainMode(boolean manual) {
        return sendCommand(COMMAND_SET_GAIN_MODE, manual ? 0x01 : 0x00);
    }

    public boolean setGain(int gain) {
        return sendCommand(COMMAND_SET_GAIN, gain);
    }

    public boolean setSoftwareGain(float gain) {
        this.softwareGain = gain;
        return true;
    }

    public boolean setSquelch(int squelch) {
        this.squelch = (float)squelch / 10000.0f;
        Log.d("DEBUG", "Squelch is " + this.squelch);
        return true;
    }

    public boolean setAGCMode(boolean enabled) {
        return sendCommand(COMMAND_SET_AGC_MODE, enabled ? 0x01 : 0x00);
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
