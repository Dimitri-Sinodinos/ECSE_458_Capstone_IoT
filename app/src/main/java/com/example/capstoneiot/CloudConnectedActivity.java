package com.example.capstoneiot;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttLastWillAndTestament;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.mobileconnectors.iot.AwsIotEndpointUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPolicyRequest;

import java.util.ArrayList;
import java.util.UUID;

public class CloudConnectedActivity extends AppCompatActivity {
    private static final String TAG = CloudConnectedActivity.class.getName();

    private static final int RESULT_SPEECH = 1;

    ImageButton btnRecord;
    Button btnPublish;
    Button btnBluetooth;
    EditText etPublish;

    AWSConnectionUtility awsConnectionUtility;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);
        setTitle("Command Center");

        btnRecord = findViewById(R.id.btnRecord);
        btnPublish = findViewById(R.id.btnPublish);
        btnBluetooth = findViewById(R.id.btnBluetooth);
        etPublish = findViewById(R.id.etPublish);

        awsConnectionUtility = AWSConnectionUtility.getInstance();
        awsConnectionUtility.initialize(getApplicationContext());

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                try {
                    startActivityForResult(intent, RESULT_SPEECH);
                    etPublish.setText("");
                }catch (ActivityNotFoundException e){
                    Toast.makeText(getApplicationContext(), "Your device doesn't support Speech to Text", Toast.LENGTH_SHORT);
                    e.printStackTrace();
                }

            }
        });

        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean success = awsConnectionUtility.publish(etPublish.getText().toString(), "CapstoneTopic");
                if(success) {
                    Toast.makeText(getApplicationContext(),"Message Published", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Could Not Publish", Toast.LENGTH_SHORT).show();
                }
                etPublish.getText().clear();
            }
        });

        btnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CloudConnectedActivity.this, DeviceScanActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case RESULT_SPEECH:
                if(resultCode == RESULT_OK && data != null){
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    etPublish.setText(text.get(0));
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        awsConnectionUtility.destroy();
        super.onDestroy();
    }
}
