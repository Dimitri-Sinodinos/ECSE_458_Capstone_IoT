package com.example.capstoneiot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPolicyRequest;
import com.amazonaws.services.iot.model.CreateThingRequest;
import com.amazonaws.services.iot.model.CreateThingResult;
import com.amplifyframework.AmplifyException;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.auth.options.AuthSignUpOptions;
import com.amplifyframework.core.Amplify;

import android.provider.Settings.Secure;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String MQTT_ENDPOINT = "a2wpi43vanpwli-ats.iot.us-east-2.amazonaws.com";
    private static final String DEVICE_ID = UUID.randomUUID().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpAmplify(this.getBaseContext());

        Button signUpButton = (Button)findViewById(R.id.goToSignup);

        Button LoginButton = (Button)findViewById(R.id.goToLogin);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(this, Signup.class);
//                startActivity(intent);
            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(this, Login.class);
//                startActivity(intent);
            }
        });
    }



//        if(signUp = true){
//            //signUp("testUsername", "testPassword", "dimitrios.sinodinos@mail.mcgill.ca");
//            confirmSignUp("testUsername", "171922");
//        }else{
//            signIn("testUsername", "testPassword");
//        }
       // signIn("capstone-amplify-user", "");

        //provisionDevice(this.getBaseContext(), DEVICE_ID);
//        connectToShadow(this.getBaseContext(), "capstoneAndroidDevice", new AWSIotMqttNewMessageCallback() {
//            @Override
//            public void onMessageArrived(String topic, byte[] data) {
//                Log.i(TAG, "Successfully connected to AWS IoT!");
//            }
//        });
//    }

    public void setUpAmplify(Context context) {
        try {
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.configure(context);
        } catch (AmplifyException e) {
            Log.e("AWSAmplify", "Exception occurred while setting up amplify:", e);
            e.printStackTrace();
        }
    }

//    public void signUp(String username, String password, String email){
//        AuthSignUpOptions options = AuthSignUpOptions.builder()
//                .userAttribute(AuthUserAttributeKey.email(), email)
//                .build();
//        Amplify.Auth.signUp(username, password, options,
//                result -> Log.i("AuthQuickStart", "Result: " + result.toString()),
//                error -> Log.e("AuthQuickStart", "Sign up failed", error)
//        );
//    }

//    public void confirmSignUp(String username, String confirmationCode){
//        Amplify.Auth.confirmSignUp(
//                username,
//                confirmationCode,
//                result -> Log.i("AuthQuickstart", result.isSignUpComplete() ? "Confirm signUp succeeded" : "Confirm sign up not complete"),
//                error -> Log.e("AuthQuickstart", error.toString())
//        );
//    }

//    public void signIn(String username, String password) {
//        Amplify.Auth.signIn(
//                username,
//                password,
//                result -> Log.i("AuthQuickstart", result.isSignInComplete() ? "Sign in succeeded" : "Sign in not complete"),
//                error -> Log.e("AuthQuickstart", error.toString())
//        );
//    }

    public void provisionDevice(Context context, String deviceId) {
        AWSMobileClient mobileClient = (AWSMobileClient) Amplify.Auth.getPlugin("awsCognitoAuthPlugin").getEscapeHatch();
        AWSIotClient client = new AWSIotClient(mobileClient);
        client.setRegion(Region.getRegion("us-east-2"));

        String deviceType = "ANDROID_DEVICE_TYPE";
        String policyName = "ANDROID_POLICY";

        mobileClient.initialize(context, new Callback<UserStateDetails>() {
            @Override
            public void onResult(final UserStateDetails details) {
                CreateThingResult newThing = client.createThing(
                        new CreateThingRequest().withThingName(deviceId).withThingTypeName(deviceType));
                client.attachPolicy(new AttachPolicyRequest().withPolicyName(policyName).withTarget(mobileClient.getIdentityId()));
            }

            @Override
            public void onError(final Exception e) {
                Log.e(TAG, "Failed to connect: ", e);
                e.printStackTrace();
            }
        });
    }

    public void connectToShadow(Context context, String deviceId, AWSIotMqttNewMessageCallback callback) {
        AWSMobileClient mobileClient = (AWSMobileClient) Amplify.Auth.getPlugin("awsCognitoAuthPlugin").getEscapeHatch();
        String topic = "$aws/things/" + deviceId + "/shadow/update/delta";
        AWSIotMqttManager mqttManager = new AWSIotMqttManager(deviceId, MQTT_ENDPOINT);
        mobileClient.initialize(context, new Callback<UserStateDetails>() {
            @Override
            public void onResult(final UserStateDetails details) {
                mqttManager.connect(mobileClient, (status, throwable) -> {
                    if (AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected.equals(status)) {
                        mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0, callback

                                /*, (topic1, data) -> {
                            callback.onMessageArrived(topic, new String(data));
                        }*/);
                    }
                });
            }

            @Override
            public void onError(final Exception e) {
                Log.e(TAG, "Failed to connect to IoT: ", e);
                e.printStackTrace();
            }
        });
    }
}