/*
 * Copyright (C) 2021 by Don F. Becker <don@donfbecker.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.donfbecker.telemetryreceiver;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class ToneDetector {
    public static final int MESSAGE_TONE_DETECTED = 20001;

    private double sampleRate;
    private double[] buffer;
    private int bufferSize;
    private double lowFrequency;
    private double highFrequency;
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
    private double maxValue = 0;

    private double magicAttenuation = 1;
    private double magicBase = 0.1;

    public ToneDetector(int sampleRate, double lowFrequency, double highFrequency, int minDurationMS) {
        this.sampleRate = sampleRate;

        this.lowFrequency = lowFrequency;
        this.highFrequency = highFrequency;

        minLength   = (int)Math.floor(sampleRate / highFrequency / 2.0);
        maxLength   = (int)Math.ceil(sampleRate / lowFrequency / 2.0);
        minDuration = (int)Math.floor((sampleRate / 1000) * minDurationMS);
        bufferSize  = Math.max(maxLength, minDuration);

        Log.d("ToneDetector", String.format("minLength=%d, maxLength=%d, minDuration=%d", minLength, maxLength, minDuration));

        buffer = new double[bufferSize];
    }

    public double filter(double input, double amplitude) {
        if((isPositive && input <= 0) || (!isPositive && input >= 0)) {
            crossCount++;

            // crossed over 0
            if(toneSamples < minLength || toneSamples > maxLength) {
                int n = Math.min(Math.max(durationSamples, toneSamples), bufferSize);
                double t = (durationSamples / sampleRate);
                double f = (crossCount / 2.0) / t;
                if(durationSamples < minDuration || f < lowFrequency || f > highFrequency) {
                    multiplySampleValues(n, 0);
                } else {
                    double s = Math.max(0, (magicBase - ((magicBase - maxAmplitude) * magicAttenuation))) / maxAmplitude;
                    Log.d("DEBUG", String.format("magicBase=%.4f maxValue=%.4f maxAmplitude=%.4f", magicBase, maxValue, maxAmplitude));
                    Log.d("DEBUG", String.format("%.0fhz tone detected lasting %.1fms with amplitude of %.4f (s=%.4f)", f, t * 1000, maxAmplitude, s));
                    //multiplySampleValues(durationSamples, s);
                    MainActivity.handler.sendMessage(MainActivity.handler.obtainMessage(ToneDetector.MESSAGE_TONE_DETECTED, (int) f, (int) (maxAmplitude * s * 1000000)));
                }

                durationSamples = 0;
                maxAmplitude = 0;
                maxValue = 0;
                crossCount = 0;
            }

            toneSamples = 0;
            isPositive = !isPositive;
        }

        if(Math.abs(input) > maxValue) maxValue = Math.abs(input);
        if(amplitude > maxAmplitude) maxAmplitude = amplitude;
        toneSamples++;
        durationSamples++;

        buffer[position] = input;
        if(++position >= bufferSize) position = 0;
        return buffer[position];
    }

    private void multiplySampleValues(int numSamples, double percentage) {
        index = position;
        for (int i = 0; i <= numSamples; i++) {
            buffer[index] *= percentage;
            if(--index < 0) index = bufferSize - 1;
        }
    }

    public void setMagicAttenuation(double attenuation) {
        magicAttenuation = attenuation;
    }

    public void setMagicBase(double base) {
        magicBase = base;
    }
}
