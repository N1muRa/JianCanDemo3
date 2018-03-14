package com.example.n1mura.jiancandemo3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by №zero on 2018/3/6.
 */

public class WelcomeActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        wasteTime();
    }

    private void wasteTime() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        MyHandler myHandler = new MyHandler();
        myHandler.sendEmptyMessage(101);
    }

    class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 101) {
                startActivity(new Intent(WelcomeActivity.this, SignInActivity.class));
                finish();
            }
        }
    }
}
