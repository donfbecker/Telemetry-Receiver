package com.donfbecker.telemetryreceiver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.donfbecker.telemetryreceiver.R;

public class MainActivity extends AppCompatActivity {
    private RtlSdrStreamer streamer;

    private int currentFrequency = 148500000;
    private int currentGain = 0;
    private int currentSquelch = 0;

    private TextView textFrequency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        streamer = new RtlSdrStreamer();

        final Button startButton = findViewById(R.id.button_start);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("DEBUG", "iqsrc://-a 127.0.0.1 -p 1234 -s " + RtlSdrStreamer.IQ_SAMPLE_RATE);
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("iqsrc://-a 127.0.0.1 -p 1234 -g 0 -f 148420440 -s " + RtlSdrStreamer.IQ_SAMPLE_RATE));
                startActivityForResult(intent, 1234);
            }
        });

        final Button stopButton = findViewById(R.id.button_stop);
        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                streamer.stop();
            }
        });

        final SeekBar gainSeekBar = findViewById(R.id.seek_gain);
        gainSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int gain, boolean b) {
                        setGain(gain);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) { }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) { }
                }
        );

        final SeekBar softwareGainSeekBar = findViewById(R.id.seek_software_gain);
        softwareGainSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int gain, boolean b) {
                        //set gain to decimal values between 1 and 5.
                        setSoftwareGain(gain);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) { }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) { }
                }
        );

        final SeekBar attenuationSeekBar = findViewById(R.id.seek_attenuation);
        attenuationSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int attenuation, boolean b) {
                        //set gain to decimal values between 1 and 5.
                        setAttenuation(attenuation);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) { }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) { }
                }
        );

        final SeekBar squelchSeekBar = findViewById(R.id.seek_squelch);
        squelchSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int squelch, boolean b) {
                        setSquelch(squelch);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) { }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) { }
                }
        );

        final Switch lowPassSwitch = findViewById(R.id.switch_lowpass);
        lowPassSwitch.setOnCheckedChangeListener(
                new Switch.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton view, boolean enabled) {
                        streamer.setFilterEnabled(enabled);
                    }
                }
        );

        final Switch toneDetectorSwitch = findViewById(R.id.switch_tone_detector);
        toneDetectorSwitch.setOnCheckedChangeListener(
                new Switch.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton view, boolean enabled) {
                        streamer.setDetectorEnabled(enabled);
                    }
                }
        );

        final Switch biasTeeSwitch = findViewById(R.id.switch_bias_tee);
        biasTeeSwitch.setOnCheckedChangeListener(
                new Switch.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton view, boolean enabled) {
                        streamer.setBiasTee(enabled);
                    }
                }
        );

        textFrequency = findViewById(R.id.text_frequency);
        textFrequency.setText(String.format("%.6f", (currentFrequency / 1000000f)));
    }

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
    }

    public void onDisableAGC(View v) {
        streamer.setAGCMode(false);
    }

    public void onEnableManualGain(View v) {
        streamer.setGainMode(true);
    }

    public void onDisableManualGain(View v) {
        streamer.setGainMode(false);
    }

    private void setFrequency(int frequency) {
        this.currentFrequency = frequency;
        textFrequency.setText(String.format("%.6f", (currentFrequency/1000000.0)));
        streamer.setFrequency(frequency);
    }

    private void setGain(int gain) {
        this.currentGain = gain;
        streamer.setGain(gain);
    }

    private void setSoftwareGain(float gain) {
        streamer.setSoftwareGain(gain);
    }

    private void setAttenuation(float attenuation) {
        streamer.setAttenuation(attenuation);
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
