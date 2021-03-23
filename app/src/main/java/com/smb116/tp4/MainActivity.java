package com.smb116.tp4;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;

public class MainActivity extends Activity {

    private final String TAG = this.getClass().getSimpleName();
    private final boolean I = true;
    private Ticker ticker;
    public static long count;
    private Receiver r1,r2,r3;

    private TextView r1Txt, r2Txt, r3Txt;
    private Button startBtn, stopBtn, finishBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.configureView();
    }

    private void configureView(){
        this.r1Txt = findViewById(R.id.r1Txt);
        this.r2Txt = findViewById(R.id.r2Txt);
        this.r3Txt = findViewById(R.id.r3Txt);
        this.startBtn = findViewById(R.id.startBtn);
        this.stopBtn = findViewById(R.id.stopBtn);
        this.finishBtn = findViewById(R.id.finishBtn);
    }

    public void onClickStart(View view){
        if(this.ticker == null){
            this.ticker = new Ticker(this);
            this.ticker.startTicker();
        }
        Log.i(TAG,"onClickStart");
    }

    public void onClickStop(View view){
        Log.i(TAG,"onClickStop");
        if(this.ticker != null){
            this.ticker.stopTicker();
            this.ticker = null;
            this.count = 0;
        }
    }

    public void onClickFinish(View view){
        if(this.ticker != null) this.ticker.stopTicker();
        finish();
    }

    //    private class Ticker extends Thread implements Serializable {  // ligne à commenter en q3
    private class Ticker extends Thread implements Parcelable{ // ligne à décommenter en q3
        public static final String TIME_ACTION_TIC = "time_action_tic";
        public static final String COUNT_EXTRA = "count";
        private Context context;


        public Ticker(Context context){
            this.context = context;
        }
        private Ticker(Parcel in){
            MainActivity.this.ticker = (Ticker)in.readValue(MainActivity.class.getClassLoader());
        }

        public final Creator<Ticker> CREATOR = new Creator<Ticker>() {
            @Override
            public Ticker createFromParcel(Parcel in) {
                return new Ticker(in);
            }

            @Override
            public Ticker[] newArray(int size) {
                return new Ticker[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeValue(this.getId());
        }

        public void startTicker(){
            this.start();
        }
        public void stopTicker(){
            this.interrupt();
        }
        public void run(){
            Intent intent = new Intent();
            intent.setAction(TIME_ACTION_TIC);
            while(!isInterrupted()){
                SystemClock.sleep(1000L);
                count++;
                intent.putExtra(Ticker.COUNT_EXTRA, count);
//                context.sendBroadcast(intent);
//                if(count < 4)                                  // à décommenter pour q2
//                context.sendBroadcast(intent);
//                else                                           // à décommenter pour q2
                context.sendOrderedBroadcast(intent,null); // à décommenter pour q2

            }
        }

    }

    public class Receiver extends BroadcastReceiver{

        private String name;

        public Receiver(String name){
            this.name = name;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            count = intent.getLongExtra(Ticker.COUNT_EXTRA, count);
            Log.i("OnReceive", name+" "+count);

            switch (name) {
                case "r1":
                    if (count > getResources().getInteger(R.integer.abort_lengt) && name.contains("r1")) {
                        this.abortBroadcast();
                        r1Txt.setText(getResources().getText(R.string.R1_text )+ " " + getResources().getInteger(R.integer.abort_lengt));
                        Log.i(TAG,"Abort: "+name+" "+getAbortBroadcast());
                    }else{
                        r1Txt.setText(getResources().getText(R.string.R1_text )+ " " + count);
                        Log.i(TAG, String.valueOf(getAbortBroadcast()));
                    }
                case "r2":
                    r2Txt.setText(getResources().getText(R.string.R2_text) + " " + count);
                case "r3":
                    r3Txt.setText(getResources().getText(R.string.R3_text) + " " + count);
                default:
                    break;
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(I) Log.i(TAG,"onResume");

        IntentFilter filter = new IntentFilter();
        filter.addAction(Ticker.TIME_ACTION_TIC);
        filter.setPriority(100);
        r1 = new Receiver("r1");
        registerReceiver(r1, filter);
        filter.setPriority(200);
        r2 = new Receiver("r2");
        registerReceiver(r2, filter);
        filter.setPriority(300);
        r3 = new Receiver("r3");
        registerReceiver(r3, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(r1);
        unregisterReceiver(r2);
        unregisterReceiver(r3);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (I) Log.i(TAG, "onSaveInstanceState");
//        outState.putSerializable("ticker", ticker); // à commenter pour q3
         outState.putParcelable("ticker",ticker); // demandé en q3
    }

    public void onRestoreInstanceState(Bundle outState){
        if(I)Log.i(TAG,"onRestoreInstanceState");
//        ticker = (Ticker)outState.getSerializable("ticker"); // à commenter pour q3
         ticker = (Ticker)outState.getParcelable("ticker"); // demandé en q3
        // suivie d'une mise à jour de l'IHM
    }
}