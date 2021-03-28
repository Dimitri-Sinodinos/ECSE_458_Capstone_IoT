package com.example.capstoneiot;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttLastWillAndTestament;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPolicyRequest;
import com.amplifyframework.core.Amplify;

import java.util.UUID;

public class Connected extends AppCompatActivity {

    private static final Regions REGION = Regions.US_EAST_2;
    private static final String ENDPOINT = "a2wpi43vanpwli-ats.iot.us-east-2.amazonaws.com";

    private static final String TAG = Connected.class.getName();

    private String policy = "MyIoTPolicy";

    String clientID;
    AWSIotMqttManager mqttManager;
    AWSIotClient iotClient;

    Button btnPublish;
    Button btnBluetooth;
    EditText etPublish;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);

        btnPublish = findViewById(R.id.btnPublish);
        btnBluetooth = findViewById(R.id.btnBluetooth);
        etPublish = findViewById(R.id.etPublish);


        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {

            @Override
            public void onResult(UserStateDetails userStateDetails) {

                Log.i(TAG, "onResult: " + userStateDetails.getUserState());

                clientID = UUID.randomUUID().toString();
                mqttManager = new AWSIotMqttManager(clientID, ENDPOINT);
                iotClient = new AWSIotClient(AWSMobileClient.getInstance());

                mqttManager.setKeepAlive(10);

                AWSIotMqttLastWillAndTestament lwt = new AWSIotMqttLastWillAndTestament("location", "Android client lost connection", AWSIotMqttQos.QOS0);

                mqttManager.setMqttLastWillAndTestament(lwt);

                iotClient.setRegion(Region.getRegion(REGION));

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AttachPolicyRequest attachPolicyReq = new AttachPolicyRequest();
                        attachPolicyReq.setPolicyName("MyIoTPolicy");
                        attachPolicyReq.setTarget(AWSMobileClient.getInstance().getIdentityId());

                        iotClient.attachPolicy(attachPolicyReq);
                    }
                }).start();

                CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                        getApplicationContext(),
                        "us-east-2:77f94997-99ed-4c30-8e8c-62d459ee3f3e", // Identity pool ID
                        Regions.US_EAST_2 // Region
                );

                mqttManager.connect(credentialsProvider, new AWSIotMqttClientStatusCallback() {
                    @Override
                    public void onStatusChanged(AWSIotMqttClientStatus status, Throwable throwable) {
                        Log.d(TAG, "Connection Status: " + String.valueOf(status));
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Initialization error.", e);
            }

        });

        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publish(etPublish.getText().toString(), "CapstoneTopic");
                etPublish.getText().clear();
            }
        });

        btnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Connected.this, DeviceScanActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        try {
            mqttManager.disconnect();
            Log.i(TAG, "Disconnected success");
        } catch (Exception e) {
            Log.e(TAG, "Disconnect error: ", e);
        }
        super.onDestroy();
    }

    public void publish(String message, String topic){
        try {
            mqttManager.publishString(message, topic, AWSIotMqttQos.QOS0);
            Toast.makeText(getApplicationContext(),"Message Published", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Publish error: ", e);
            Toast.makeText(getApplicationContext(),"Could Not Publish", Toast.LENGTH_SHORT).show();
        }
    }
}
