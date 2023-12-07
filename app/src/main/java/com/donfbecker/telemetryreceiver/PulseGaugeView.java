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

    private double amplitude;

    private Paint barBrush;

    public PulseGaugeView(Context context, AttributeSet attributes) {
        super(context, attributes);

        barBrush = new Paint();
        barBrush.setStyle(Paint.Style.FILL);
        barBrush.setColor(Color.parseColor("#008800"));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, (float)((canvas.getWidth()) * amplitude), canvas.getHeight(), barBrush);
    }

    @Override
    protected void onMeasure(int width, int height) {
        setMeasuredDimension(width, height);
    }

    public void addPulse(double amplitude) {
        Log.d("DEBUG", "Adding pulse with amplitude of " + amplitude);
        if(amplitude > this.amplitude) this.amplitude = amplitude;
        else this.amplitude *= 0.9;
        invalidate();
    }
}
