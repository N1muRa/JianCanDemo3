package com.example.n1mura.jiancandemo3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;

import java.io.IOException;

import Model.User;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by №zero on 2018/3/6.
 */

public class SignUpActivity extends Activity {


    private String urlStr = "http://111.231.140.88/api/SignIn";
    private Button btn_signUp;
    private Button btn_back;
    private EditText editText1;
    private EditText editText2;
    final OkHttpClient client = new OkHttpClient();

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message message){
            if (message.what == 1){
                String res = (String) message.obj;
                Log.i("ss", res);
                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                finish();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        editText1 = (EditText) findViewById(R.id.username);
        editText2 = (EditText) findViewById(R.id.password);
        btn_signUp = (Button) findViewById(R.id.btn_signUp);
        btn_back = (Button) findViewById(R.id.btn_back_sign_in);

        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = editText1.getText().toString();
                String password = editText2.getText().toString();
//                postRequest(username, password);
                handler.obtainMessage(1, "注册成功").sendToTarget();
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void postRequest(String username, String password){
        User user = new User(username, password);
        Gson gson = new Gson();
        String json = gson.toJson(user);
        Log.i("json", json);

        RequestBody body = FormBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        final Request request = new Request.Builder()
                .url(urlStr)
                .post(body)
                .build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                    if (response.isSuccessful()){
                        handler.obtainMessage(1, response.body().string()).sendToTarget();
                    } else {
                        throw new IOException("Unexpected code: " + response);
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
