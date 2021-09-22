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

    private TextView textFrequency;

    private static PulseGaugeView pulseGauge;
    private static PulseCompassView pulseCompass;

    public static Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            switch(message.what) {
                case ToneDetector.MESSAGE_TONE_DETECTED:
                    pulseGauge.addPulse(message.arg2 / 1000000.0);
                    pulseCompass.addPulse(message.arg2 / 1000000.0);
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

        streamer = new RtlSdrStreamer();

        final Button startButton = findViewById(R.id.button_start);
        startButton.setOnClickListener(this);

        final Button stopButton = findViewById(R.id.button_stop);
        stopButton.setOnClickListener(this);

        final SeekBar gainSeekBar = findViewById(R.id.seek_gain);
        gainSeekBar.setOnSeekBarChangeListener(this);

        final SeekBar softwareGainSeekBar = findViewById(R.id.seek_software_gain);
        softwareGainSeekBar.setOnSeekBarChangeListener(this);

        final SeekBar attenuationSeekBar = findViewById(R.id.seek_attenuation);
        attenuationSeekBar.setOnSeekBarChangeListener(this);

        final SeekBar magicAttenuationSeekBar = findViewById(R.id.seek_magic_attenuation);
        magicAttenuationSeekBar.setOnSeekBarChangeListener(this);

        final SeekBar magicBaseSeekBar = findViewById(R.id.seek_magic_base);
        magicBaseSeekBar.setOnSeekBarChangeListener(this);

        final Switch lowPassSwitch = findViewById(R.id.switch_lowpass);
        lowPassSwitch.setOnCheckedChangeListener(this);

        final Switch toneDetectorSwitch = findViewById(R.id.switch_tone_detector);
        toneDetectorSwitch.setOnCheckedChangeListener(this);

        final Switch biasTeeSwitch = findViewById(R.id.switch_bias_tee);
        biasTeeSwitch.setOnCheckedChangeListener(this);

        textFrequency = findViewById(R.id.text_frequency);
        textFrequency.setText(String.format("%.6f", (currentFrequency / 1000000f)));

        pulseGauge = findViewById(R.id.pulse_gauge);
        pulseCompass = findViewById(R.id.pulse_compass);


    }

    //
    // Button events, or anything with a click I guess
    //
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.button_start:
                Log.d("DEBUG", "iqsrc://-a 127.0.0.1 -p 1234 -s " + RtlSdrStreamer.IQ_SAMPLE_RATE);
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("iqsrc://-a 127.0.0.1 -p 1234 -g 0 -f 148420440 -s " + RtlSdrStreamer.IQ_SAMPLE_RATE));
                startActivityForResult(intent, 1234);
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
                setGain(value);
                break;

            case R.id.seek_software_gain:
                setSoftwareGain(value);
                break;

            case R.id.seek_attenuation:
                setAttenuation(value);
                break;

            case R.id.seek_magic_attenuation:
                streamer.setMagicAttenuation(value / 100.0);
                break;

            case R.id.seek_magic_base:
                streamer.setMagicBase(value / 100.0);
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
            case R.id.switch_bias_tee:
                streamer.setBiasTee(enabled);
                break;
            case R.id.switch_lowpass:
                streamer.setFilterEnabled(enabled);
                break;
            case R.id.switch_tone_detector:
                streamer.setDetectorEnabled(enabled);
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

    public void onEnableAGC(View v) {
        streamer.setAGCMode(true);
        pulseCompass.reset();
    }

    public void onDisableAGC(View v) {
        streamer.setAGCMode(false);
        pulseCompass.reset();
    }

    public void onEnableManualGain(View v) {
        streamer.setGainMode(true);
        pulseCompass.reset();
    }

    public void onDisableManualGain(View v) {
        streamer.setGainMode(false);
        pulseCompass.reset();
    }

    private void setFrequency(int frequency) {
        this.currentFrequency = frequency;
        textFrequency.setText(String.format("%.6f", (currentFrequency/1000000.0)));
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

        // Invalid arguments cause the driver to stop
        Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("iqsrc://-x"));
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("DEBUG", "onActivityResult(" + requestCode + ", " + resultCode + ")");

        if (requestCode != 1234) return; // This is the requestCode that was used with startActivityForResult
        if (resultCode == RESULT_OK) {
            // Connection with device has been opened and the rtl-tcp server is running. You are now responsible for connecting.
            int[] supportedTcpCommands = data.getIntArrayExtra("supportedTcpCommands");
            streamer.start();
            streamer.setFrequency(currentFrequency);
            streamer.setGain(currentGain);
        } else {
            // something went wrong, and the driver failed to start
            String message = data.getStringExtra("detailed_exception_message");
            Log.d("ERROR", message); // Show the user why something went wrong
        }
    }
}
