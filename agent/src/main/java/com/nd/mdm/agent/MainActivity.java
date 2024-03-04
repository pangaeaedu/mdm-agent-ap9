package com.nd.mdm.agent;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.nd.mdm.communicate.PushModule;

public class MainActivity extends Activity {
    private PushModule mPushModule = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button btnConnectPush = null;
        String pushIp = "push.dev.apse1.ndpg.xyz";
        setContentView(R.layout.activity_main);

        btnConnectPush = findViewById(R.id.btn_connectPush);
        btnConnectPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPushModule.start();
            }

        });
    }

}