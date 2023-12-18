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