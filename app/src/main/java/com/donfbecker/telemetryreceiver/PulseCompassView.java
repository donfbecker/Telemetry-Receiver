package com.donfbecker.telemetryreceiver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class PulseCompassView extends View {
    private final int SIZE = 360;

    private double[] pulses;
    private double[] bearings;
    private int currentBearing = 0;

    public PulseCompassView(Context context, AttributeSet attributes) {
        super(context, attributes);

        pulses = new double[SIZE];
        bearings = new double[5];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();

        Paint brush = new Paint();
        brush.setStyle(Paint.Style.STROKE);
        brush.setColor(Color.parseColor("#888888"));

        canvas.save();
        canvas.rotate((float)(-90 - currentBearing), w/2, h/2);

        // Draw cardinal direction cross
        canvas.drawLine(w / 2, 0, w/2, h, brush);
        canvas.drawLine(0, h/2, w, h/2, brush);

        // Draw rings
        float r = h / 7;
        canvas.drawCircle(w/2, h/2, r, brush);
        canvas.drawCircle(w/2, h/2, r*2, brush);
        canvas.drawCircle(w/2, h/2, r*3, brush);

        // Draw gauge lines
        Path path = new Path();
        float x = Math.round((w/2) + ((1 + pulses[SIZE-1]) * r * Math.cos(Math.toRadians(SIZE-1))));
        float y = Math.round((h/2) + ((1 + pulses[SIZE-1]) * r * Math.sin(Math.toRadians(SIZE-1))));
        path.moveTo(x, y);

        for(int a = 0; a < 360; a++) {
            x = Math.round((w/2) + ((1 + pulses[a]) * r * Math.cos(Math.toRadians(a))));
            y = Math.round((h/2) + ((1 + pulses[a]) * r * Math.sin(Math.toRadians(a))));
            path.lineTo(x, y);
        }
        path.close();

        brush.setStrokeWidth(5);
        brush.setColor(Color.parseColor("#0000ff"));
        canvas.drawPath(path, brush);

        canvas.restore();

    }

    @Override
    protected void onMeasure(int width, int height) {
        int d = Math.max(width, height);
        setMeasuredDimension(d, d);
    }

    public void setBearing(double bearing) {
        double sum = 0;
        for(int i = 5-1; i > 0; i--) {
            sum += bearings[i];
            bearings[i] = bearings[i - 1];
        }
        sum += bearing;
        bearings[0] = bearing;
        currentBearing = (int)Math.round(sum/5);
        invalidate();
    }

    public void addPulse(double amplitude) {
        Log.d("DEBUG", "Adding pulse with amplitude of " + amplitude + " at " + currentBearing + " degrees.");
        pulses[currentBearing] = amplitude;
        invalidate();
    }

    public void reset() {
        for(int i = 0; i < SIZE; i++) pulses[i] = 0;
        invalidate();
    }
}
