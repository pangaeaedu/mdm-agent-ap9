package com.example.mdm_agent_ap9;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mdm_agent_ap9.communicate.impl.MdmTransferFactory;

public class MainActivity extends AppCompatActivity {

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
                MdmTransferFactory.getPushModel().start();
            }
        });


    }
}