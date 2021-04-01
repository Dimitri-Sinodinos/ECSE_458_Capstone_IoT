package com.example.capstoneiot;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttLastWillAndTestament;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPolicyRequest;

import java.util.UUID;

public class AWSConnectionUtility {

    private static final String TAG = AWSConnectionUtility.class.getName();
    private static final Regions REGION = Regions.US_EAST_2;
    private static final String ENDPOINT = "a2wpi43vanpwli-ats.iot.us-east-2.amazonaws.com";

    private String policy = "MyIoTPolicy";

    String clientID;
    AWSIotMqttManager mqttManager;
    AWSIotClient iotClient;


    public AWSConnectionUtility(Context applicationContext) {
        AWSMobileClient.getInstance().initialize(applicationContext, new Callback<UserStateDetails>() {

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
                        attachPolicyReq.setPolicyName(policy);
                        attachPolicyReq.setTarget(AWSMobileClient.getInstance().getIdentityId());

                        iotClient.attachPolicy(attachPolicyReq);
                    }
                }).start();

                CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                        applicationContext,
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
    }

    public boolean publish(String message, String topic){
        try {
            mqttManager.publishString(message, topic, AWSIotMqttQos.QOS0);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Publish error: ", e);
            return false;
        }
    }

    public void destroy() {
        try {
            boolean success = mqttManager.disconnect();
            Log.i(TAG, "Disconnected status: " + success);
        } catch (Exception e) {
            Log.e(TAG, "Disconnect error: ", e);
        }
    }
}
