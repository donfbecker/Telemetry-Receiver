package com.donfbecker.telemetryreceiver;

public class ToneDetector {
    private float[] buffer;
    private int bufferSize;
    private int minLength;
    private int maxLength;

    private boolean isPositive = true;
    private int toneSamples = 0;

    public ToneDetector(double sampleRate, double lowFrequency, double highFrequency) {
        minLength = (int)Math.floor(sampleRate * (1 / highFrequency / 2));
        maxLength = (int)Math.ceil(sampleRate * (1 / lowFrequency / 2));
        bufferSize = maxLength;

        buffer = new float[bufferSize];
    }

    public float filter(float input) {
        int i;

        if((isPositive && input < 0) || (!isPositive && input > 0)) {
            // crossed over 0
            if(toneSamples < minLength || toneSamples > maxLength) {
                for(i = (bufferSize - toneSamples); i < bufferSize; i++) buffer[i] = 0;
            }

            toneSamples = 0;
            isPositive = !isPositive;
        }

        for(i = 0; i < bufferSize - 1; i++) buffer[i] = buffer[i + 1];
        toneSamples++;
        buffer[bufferSize - 1] = input;
        return buffer[0];
    }
}
