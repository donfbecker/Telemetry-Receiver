package com.donfbecker.telemetryreceiver;

import com.donfbecker.rtlsdr.RtlSdr;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.lang.Thread;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.util.Log;

// I think I should sample at 240kHz.  That is a multiple of 5 to reduce
// down to the audio sample rate of 48kHz.  If we read 320 samples each
// time, that reduces down to 64 audio samples, and represents about
// 1.3ms of time.  8 of those sample blocks would represent 10.4ms of
// pulse data, can can be combined to 512 samples to run though FFT.

public class RtlSdrStreamer {
    // Sample rate must be between 225001 and 300000 or 900001 and 3200000
    public static final int IQ_SAMPLE_RATE = 240000;
    public static final int AUDIO_SAMPLE_RATE = 48000;
    public static final int SAMPLE_RATIO = 5;

    public static final double SQRT_TWO = Math.sqrt(2.0);

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

    public static final int MESSAGE_SIGNAL_STRENGTH       = 10001;

    public static final int[] GAIN_VALUES = {0, 9, 14, 27, 37, 77, 87, 125, 144, 157, 166, 197, 207, 229, 254, 280, 297, 328, 338, 364, 372, 386, 402, 421, 434, 439, 445, 480, 496};

    private RtlSdr device;

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

    private ArrayBlockingQueue<byte[]> commandQueue = null;

    private double d_table[];

    private int blocks = 0;
    private double blockPowerMax;

    public RtlSdrStreamer(Context ctx) {
        commandQueue = new ArrayBlockingQueue<byte[]>(100);
        iFilter = new RCFilter(RCFilter.FILTER_LOWPASS, 24000.0d, 1.0d/IQ_SAMPLE_RATE);
        qFilter = new RCFilter(RCFilter.FILTER_LOWPASS, 24000.0d, 1.0d/IQ_SAMPLE_RATE);

        // Initialize byte to double table
        d_table = new double[256];
        for(int i = 0; i < 256; i++) {
            d_table[i] = (double)(((double)i - 127.5f) / 127.5f);
        }

        RtlSdr.initialize(ctx);
    }

    public boolean isRunning() {
        return false;
    }

    public boolean start() {
        Log.d("RtlSdrStreamer", "RtlSdrStreamer.start()");

        try {
            int count = RtlSdr.getDeviceCount();
            Log.d("RtlSdrStreamer", "Available devices: " + count);
            for(int i = 0; i < count; i++) {
                String[] strings = RtlSdr.getDeviceUsbStrings(i);
                Log.d("RtlSdrStreamer", i + ": " + strings[1] + " (" + strings[2] + ")");
            }
        } catch (Exception e) {

        }

        return true;
    }

    public boolean stop() {
        Log.d("DEBUG", "Stop requested");
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
