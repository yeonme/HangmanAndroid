package me.yeon.hangmanandroid;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.time.Duration;
import java.util.Calendar;
import java.util.TimeZone;

import me.yeon.hangmanandroid.comm.DataComm;

/**
 * Created by yeon on 2018-03-08 008.
 */

public class PlayActivity extends AppCompatActivity {

    private int mInterval = 25; // 5 seconds by default, can be changed later
    private Calendar dueTime;
    private Handler mHandler;

    private ProgressBar pbTime;
    private TextView tvTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        setTitle("Game Activity");

        pbTime = findViewById(R.id.pbTime);
        pbTime.setMax(20000);
        tvTime = findViewById(R.id.tvTime);
        dueTime = Calendar.getInstance();
        dueTime.add(Calendar.SECOND,20);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mHandler = new Handler();
        mStatusChecker.run();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                timerJob(); //this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    private void timerJob() {
        int remaining = (int)(dueTime.getTimeInMillis() - Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis() - DataComm.getInstance().timeAdv);
        remaining = Math.max(0,Math.min(20000,remaining));
        pbTime.setProgress(pbTime.getMax()-remaining);
        tvTime.setText(String.format("%1.3f", remaining/1000.0));
    }

}
