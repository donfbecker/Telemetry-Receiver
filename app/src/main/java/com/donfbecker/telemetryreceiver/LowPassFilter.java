package com.donfbecker.telemetryreceiver;

public class LowPassFilter {
    private final int SAMPLE_SIZE = 40;
    private float[] coefficients = {0.0006920f,0.0009442f,0.0013667f,0.0020309f,0.0030003f,0.0043263f,0.0060426f,0.0081621f,0.0106741f,0.0135433f,0.0167100f,0.0200920f,0.0235883f,0.0270835f,0.0304538f,0.0335729f,0.0363196f,0.0385836f,0.0402719f,0.0413143f,0.0416667f,0.0413143f,0.0402719f,0.0385836f,0.0363196f,0.0335729f,0.0304538f,0.0270835f,0.0235883f,0.0200920f,0.0167100f,0.0135433f,0.0106741f,0.0081621f,0.0060426f,0.0043263f,0.0030003f,0.0020309f,0.0013667f,0.0009442f};
    private float[] buffer;

    public LowPassFilter() {
        this.buffer = new float[SAMPLE_SIZE];
        for(int i = 0; i < SAMPLE_SIZE; i++) this.buffer[i] = 0;
    }

    public float filter(float input) {
        float v = 0;

        for(int i = SAMPLE_SIZE - 1; i > 0; i--) {
            buffer[i] = buffer[i - 1];
            v += buffer[i] * coefficients[SAMPLE_SIZE - i - 1];
        }

        buffer[0] = input;
        v += input * coefficients[SAMPLE_SIZE - 1];
        return v;
    }
}
