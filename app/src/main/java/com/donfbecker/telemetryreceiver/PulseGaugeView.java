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
        if(amplitude > this.amplitude) this.amplitude = amplitude;
        else this.amplitude *= 0.9;
        invalidate();
    }
}
