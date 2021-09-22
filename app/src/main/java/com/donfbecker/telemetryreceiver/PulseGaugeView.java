package com.donfbecker.telemetryreceiver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class PulseGaugeView extends View {
    private final int SIZE = 5;

    private double[] pulses;

    public PulseGaugeView(Context context, AttributeSet attributes) {
        super(context, attributes);

        pulses = new double[SIZE];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint brush = new Paint();
        brush.setStyle(Paint.Style.FILL);
        brush.setColor(Color.parseColor("#008800"));

        float h = canvas.getHeight() / SIZE;

        for(int i = 0; i < SIZE; i++) {
            canvas.drawRect(0, i * h, (float)(canvas.getWidth() * pulses[i]), (i + 1) * h, brush);
        }
    }

    @Override
    protected void onMeasure(int width, int height) {
        setMeasuredDimension(width, height);
    }

    public void addPulse(double amplitude) {
        Log.d("DEBUG", "Adding pulse with amplitude of " + amplitude);
        for(int i = SIZE - 1; i > 0; i--) pulses[i] = pulses[i - 1];
        pulses[0] = amplitude;

        invalidate();
    }
}
