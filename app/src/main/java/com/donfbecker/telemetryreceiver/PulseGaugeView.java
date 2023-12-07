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

    private Paint barBrush;
    private Paint textBrush;

    public PulseGaugeView(Context context, AttributeSet attributes) {
        super(context, attributes);

        pulses = new double[SIZE];

        barBrush = new Paint();
        barBrush.setStyle(Paint.Style.FILL);
        barBrush.setColor(Color.parseColor("#008800"));

        textBrush = new Paint();
        textBrush.setStyle(Paint.Style.FILL);
        textBrush.setColor(Color.parseColor("#000000"));

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float h = canvas.getHeight() / SIZE;
        textBrush.setTextSize(h);

        for(int i = 0; i < SIZE; i++) {
            canvas.drawRect(55, i * h, 55 + (float)((canvas.getWidth()-55) * pulses[i]), (i + 1) * h, barBrush);
            canvas.drawText(String.format("%.2f", pulses[i]), 0, (i+1)*h, textBrush);
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
