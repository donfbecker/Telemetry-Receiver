package com.donfbecker.telemetryreceiver;

public class RCFilter {
    public static final int FILTER_LOWPASS = 0;
    public static final int FILTER_HIGHPASS = 1;

    private int type;
    private double coeff1, coeff2;
    private double out1, out2;

    public RCFilter(int type, double cutOffFreqHz, double sampleTimeSec) {
        this.type = type;
        this.out1 = 0;
        this.out2 = 0;

        double RC = 1.0f / (Math.PI * 2 * cutOffFreqHz);

        switch(type) {
            case FILTER_LOWPASS:
                this.coeff1 = sampleTimeSec / (RC + sampleTimeSec);
                this.coeff2 = RC / (RC + sampleTimeSec);
                break;

            case FILTER_HIGHPASS:
                this.coeff1 = RC / (RC + sampleTimeSec);
                this.coeff2 = 1.0f;
                break;
        }
    }

    public double filter(double sample) {
        this.out2 = this.out1;

        switch(this.type) {
            case FILTER_LOWPASS:
                this.out1 = (this.coeff1 * sample) + (this.coeff2 * this.out2);
                break;

            case FILTER_HIGHPASS:
                this.out1 = (this.coeff1 * this.out2) + (this.coeff1 * (sample - this.coeff2));
                this.coeff2 = sample;
                break;
        }

        return this.out1;
    }
}