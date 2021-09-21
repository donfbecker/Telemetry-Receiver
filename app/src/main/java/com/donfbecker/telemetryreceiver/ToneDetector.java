package com.donfbecker.telemetryreceiver;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class ToneDetector {
    public static final int MESSAGE_TONE_DETECTED = 10001;

    private int sampleRate;
    private double[] buffer;
    private int bufferSize;
    private int minLength;
    private int maxLength;
    private int minDuration;

    private boolean isPositive = true;
    private int toneSamples = 0;
    private int durationSamples = 0;

    private int position = 0;
    private int index = 0;

    private int crossCount = 0;
    private double maxAmplitude = 0;

    public ToneDetector(int sampleRate, double lowFrequency, double highFrequency, int minDurationMS) {
        this.sampleRate = sampleRate;

        minLength   = (int)Math.floor(sampleRate * (1 / highFrequency / 2));
        maxLength   = (int)Math.ceil(sampleRate * (1 / lowFrequency / 2));
        minDuration = (int)Math.floor((sampleRate / 1000) * minDurationMS);
        bufferSize  = Math.max(maxLength, minDuration);

        buffer = new double[bufferSize];
    }

    public double filter(double input, double amplitude) {
        int i;

        if((isPositive && input < 0) || (!isPositive && input > 0)) {
            crossCount++;

            // crossed over 0
            if(toneSamples < minLength || toneSamples > maxLength) {
                if(durationSamples < minDuration) {
                    index = position;
                    int n = Math.min(Math.max(durationSamples, toneSamples), bufferSize);
                    for (i = 0; i <= n; i++) {
                        buffer[index] = 0;
                        if(--index < 0) index = bufferSize - 1;
                    }
                } else {
                    double t = (durationSamples / (sampleRate / 1.0));
                    double f = (crossCount / 2.0) / t;
                    Log.d("DEBUG", String.format("%.0fhz tone detected lasting %.1fms with amplitude of %.4f", f, t*1000, maxAmplitude));
                    MainActivity.handler.sendMessageDelayed(MainActivity.handler.obtainMessage(ToneDetector.MESSAGE_TONE_DETECTED, (int)f, (int)(maxAmplitude*1000000)), (int)t);
                }

                durationSamples = 0;
                maxAmplitude = 0;
                crossCount = 0;
            }

            toneSamples = 0;
            isPositive = !isPositive;
        }

        //if(Math.abs(input) > maxAmplitude) maxAmplitude = Math.abs(input);
        if(amplitude > maxAmplitude) maxAmplitude = amplitude;
        toneSamples++;
        durationSamples++;

        buffer[position] = input;
        if(++position >= bufferSize) position = 0;
        return buffer[position];
    }
}
