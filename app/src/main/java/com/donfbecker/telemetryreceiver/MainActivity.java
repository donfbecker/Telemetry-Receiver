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

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.donfbecker.telemetryreceiver.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, Switch.OnCheckedChangeListener {

    private RtlSdrStreamer streamer;

    private int currentFrequency = 148500000;
    private int currentGain = 0;
    private int currentSquelch = 0;

    private Button startButton;
    private Button stopButton;
    private TextView frequencyText;
    private SeekBar gainSeekBar;
    private TextView gainSeekBarValue;
    private SeekBar attenuationSeekBar;
    private TextView attenuationSeekBarValue;

    private Switch enableAGCSwitch;
    private Switch toneDetectorSwitch;
    private Switch biasTeeSwitch;

    private static PulseGaugeView pulseGauge;
    private static PulseCompassView pulseCompass;



    public static Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            switch(message.what) {
                case RtlSdrStreamer.MESSAGE_SIGNAL_STRENGTH:
                    double power = message.arg1 / 1000000.0;
                    pulseGauge.addPulse(power);
                    pulseCompass.addPulse(power);
                    break;

                default:
                    Log.d("DEBUG", "Unknown message: " + message.what);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        streamer = new RtlSdrStreamer(getApplicationContext());

        startButton = findViewById(R.id.button_start);
        startButton.setOnClickListener(this);

        stopButton = findViewById(R.id.button_stop);
        stopButton.setOnClickListener(this);

        frequencyText = findViewById(R.id.text_frequency);
        frequencyText.setText(String.format("%.6f", (currentFrequency / 1000000f)));

        pulseGauge = findViewById(R.id.pulse_gauge);
        pulseCompass = findViewById(R.id.pulse_compass);

        gainSeekBar = findViewById(R.id.seek_gain);
        gainSeekBar.setMax(RtlSdrStreamer.GAIN_VALUES.length - 1);
        gainSeekBar.setOnSeekBarChangeListener(this);
        gainSeekBarValue = findViewById(R.id.text_gain_value);

        attenuationSeekBar = findViewById(R.id.seek_attenuation);
        attenuationSeekBar.setOnSeekBarChangeListener(this);
        attenuationSeekBarValue = findViewById(R.id.text_attenuation_value);

        enableAGCSwitch = findViewById(R.id.switch_enable_agc);
        enableAGCSwitch.setOnCheckedChangeListener(this);

        biasTeeSwitch = findViewById(R.id.switch_bias_tee);
        biasTeeSwitch.setOnCheckedChangeListener(this);
    }

    //
    // Button events, or anything with a click I guess
    //
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.button_start:
                streamer.start();
                break;

            case R.id.button_stop:
                streamer.stop();
                break;
        }
    }

    //
    // SeekBar events
    //
    public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
        switch(seekBar.getId()) {
            case R.id.seek_gain:
                setGain(RtlSdrStreamer.GAIN_VALUES[value]);
                gainSeekBarValue.setText(String.format("%.1fdb", (RtlSdrStreamer.GAIN_VALUES[value]/10.0)));
                break;

            case R.id.seek_attenuation:
                setAttenuation(value);
                double db = (value == 0) ? 0 : Math.log10(1.0 - (value / 100.0)) * 20;
                attenuationSeekBarValue.setText(String.format("%.1fdb", db));
                break;
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) { }
    public void onStopTrackingTouch(SeekBar seekBar) { }

    //
    // Switch events
    //
    public void onCheckedChanged(CompoundButton view, boolean enabled) {
        switch (view.getId()) {
            case R.id.switch_enable_agc:
                streamer.setAGCMode(enabled);
                pulseCompass.reset();
                break;
            case R.id.switch_bias_tee:
                streamer.setBiasTee(enabled);
                break;
        }
    }

    //
    // Other functions
    //

    public void onIncreaseFrequency(View v) {
        int n = Integer.parseInt(getResources().getResourceEntryName(v.getId()).replace("button_plus_", ""));
        setFrequency(currentFrequency + n);
    }

    public void onDecreaseFrequency(View v) {
        int n = Integer.parseInt(getResources().getResourceEntryName(v.getId()).replace("button_minus_", ""));
        setFrequency(currentFrequency - n);
    }

    public void onSetFrequency(View v) {
        int n = Integer.parseInt(getResources().getResourceEntryName(v.getId()).replace("button_frequency_", ""));
        setFrequency(n);
    }

    private void setFrequency(int frequency) {
        this.currentFrequency = frequency;
        frequencyText.setText(String.format("%.6f", (currentFrequency/1000000.0)));
        streamer.setFrequency(frequency);
    }

    private void setGain(int gain) {
        this.currentGain = gain;
        streamer.setGain(gain);
        pulseCompass.reset();
    }

    private void setSoftwareGain(float gain) {
        streamer.setSoftwareGain(gain);
        pulseCompass.reset();
    }

    private void setAttenuation(int attenuation) {
        streamer.setAttenuation(attenuation);
        pulseCompass.reset();
    }

    private void setSquelch(int squelch) {
        this.currentSquelch = squelch;
        streamer.setSquelch(squelch);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        streamer.stop();
    }
}
