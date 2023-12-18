package com.donfbecker.telemetryreceiver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class PulseCompassView extends View implements SensorEventListener {
    private final int SIZE = 360;
    private final int BEARING_AVERAGE_SIZE = 50;

    private SensorManager sensorManager;
    private Sensor sensorRotation;

    private float[] orientationMatrix = new float[3];
    private float[] rotationMatrix = new float[9];

    private double[] pulses;
    private double[] bearings;
    private int currentBearing = 0;

    public PulseCompassView(Context context, AttributeSet attributes) {
        super(context, attributes);

        pulses = new double[SIZE];
        bearings = new double[BEARING_AVERAGE_SIZE];

        sensorManager  = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        sensorRotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorManager.registerListener(this, sensorRotation, SensorManager.SENSOR_DELAY_NORMAL);
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
        canvas.rotate((float)(-currentBearing), w/2, h/2);

        // Draw rings
        float r = h / 7;
        canvas.drawCircle(w/2, h/2, r, brush);
        canvas.drawCircle(w/2, h/2, r*2, brush);
        canvas.drawCircle(w/2, h/2, r*3, brush);

        // Draw cardinal direction cross
        canvas.drawLine(w / 2, 0, w/2, h, brush);
        canvas.drawLine(0, h/2, w, h/2, brush);
        brush.setTextAlign(Paint.Align.CENTER);
        brush.setTextSize(50);
        canvas.drawText("N", w/2, (h/2) - r*3, brush);

        // Draw gauge lines
        Path path = new Path();
        float x = Math.round((w/2) + ((1 + pulses[SIZE-1]) * r * Math.cos(Math.toRadians(SIZE- 1 - 90))));
        float y = Math.round((h/2) + ((1 + pulses[SIZE-1]) * r * Math.sin(Math.toRadians(SIZE- 1 - 90))));
        path.moveTo(x, y);

        for(int a = 0; a < 360; a++) {
            x = Math.round((w/2) + ((1 + pulses[a]*2) * r * Math.cos(Math.toRadians(a - 90))));
            y = Math.round((h/2) + ((1 + pulses[a]*2) * r * Math.sin(Math.toRadians(a - 90))));
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

    public int getBearing() {
        return currentBearing;
    }

    public void setBearing(double bearing) {
        currentBearing = (int)Math.round(bearing);
        invalidate();
    }

    public void addPulse(double amplitude) {
        if(amplitude > pulses[currentBearing]) pulses[currentBearing] = amplitude;
        invalidate();
    }

    public void reset() {
        for(int i = 0; i < SIZE; i++) pulses[i] = 0;
        invalidate();
    }

    //
    // Sensor events
    //
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()) {
            case Sensor.TYPE_ROTATION_VECTOR:
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
                SensorManager.getOrientation(rotationMatrix, orientationMatrix);

                double degrees = (double)(Math.toDegrees(orientationMatrix[0]) + 360) % 360;
                setBearing(degrees);
                break;
        }
    }
}
