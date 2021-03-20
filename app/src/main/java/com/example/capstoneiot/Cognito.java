package com.example.capstoneiot;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.amazonaws.regions.Regions;
import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amplifyframework.auth.options.AuthSignUpOptions;
import com.amplifyframework.core.Amplify;

import static android.content.ContentValues.TAG;

public class Cognito {

    private String poolID = "Place_Your_UserPoolID";
    private String clientID = "Place_Your_ClientID";
    private String clientSecret = "Place_Your_ClientSecret";
    private Regions awsRegion = Regions.US_EAST_2;

    private CognitoUserPool userPool;
    private CognitoUserAttributes userAttributes;       // Used for adding attributes to the user
    private Context appContext;

    private String userPassword;

    public Cognito(Context context){
        appContext = context;
        userPool = new CognitoUserPool(context, this.poolID, this.clientID, this.clientSecret, this.awsRegion);
        userAttributes = new CognitoUserAttributes();
    }

    public void signUp(String username, String password, String email){
        AuthSignUpOptions options = AuthSignUpOptions.builder()
                .userAttribute(AuthUserAttributeKey.email(), email)
                .build();
        Amplify.Auth.signUp(username, password, options,
                result -> Log.i("AuthQuickStart", "Result: " + result.toString()),
                error -> Log.e("AuthQuickStart", "Sign up failed", error)
        );
    }

//    public void signUpInBackground(String userId, String password){
//        userPool.signUpInBackground(userId, password, this.userAttributes, null, signUpCallback);
//        //userPool.signUp(userId, password, this.userAttributes, null, signUpCallback);
//    }
//
//    SignUpHandler signUpCallback = new SignUpHandler() {
//        @Override
//        public void onSuccess(CognitoUser cognitoUser, boolean userConfirmed, CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
//            // Sign-up was successful
//            Log.d(TAG, "Sign-up success");
//            Toast.makeText(appContext,"Sign-up success", Toast.LENGTH_LONG).show();
//            // Check if this user (cognitoUser) needs to be confirmed
//            if(!userConfirmed) {
//                // This user must be confirmed and a confirmation code was sent to the user
//                // cognitoUserCodeDeliveryDetails will indicate where the confirmation code was sent
//                // Get the confirmation code from user
//            }
//            else {
//                Toast.makeText(appContext,"Error: User Confirmed before", Toast.LENGTH_LONG).show();
//                // The user has already been confirmed
//            }
//        }
//        @Override
//        public void onFailure(Exception exception) {
//            Toast.makeText(appContext,"Sign-up failed", Toast.LENGTH_LONG).show();
//            Log.d(TAG, "Sign-up failed: " + exception);
//        }
//    };

    public void confirmSignUp(String username, String confirmationCode){
        Amplify.Auth.confirmSignUp(
                username,
                confirmationCode,
                result -> Log.i("AuthQuickstart", result.isSignUpComplete() ? "Confirm signUp succeeded" : "Confirm sign up not complete"),
                error -> Log.e("AuthQuickstart", error.toString())
        );
    }

//    public void confirmUser(String userId, String code){
//        CognitoUser cognitoUser =  userPool.getUser(userId);
//        cognitoUser.confirmSignUpInBackground(code,false, confirmationCallback);
//        //cognitoUser.confirmSignUp(code,false, confirmationCallback);
//    }
//    // Callback handler for confirmSignUp API
//    GenericHandler confirmationCallback = new GenericHandler() {
//
//        @Override
//        public void onSuccess() {
//            // User was successfully confirmed
//            Toast.makeText(appContext,"User Confirmed", Toast.LENGTH_LONG).show();
//
//        }
//
//        @Override
//        public void onFailure(Exception exception) {
//            // User confirmation failed. Check exception for the cause.
//
//        }
//    };

    public void addAttribute(String key, String value){
        userAttributes.addAttribute(key, value);
    }

    public void signIn(String username, String password) {
        Amplify.Auth.signIn(
                username,
                password,
                result -> Log.i("AuthQuickstart", result.isSignInComplete() ? "Sign in succeeded" : "Sign in not complete"),
                error -> Log.e("AuthQuickstart", error.toString())
        );
    }
//    public void userLogin(String userId, String password){
//        CognitoUser cognitoUser =  userPool.getUser(userId);
//        this.userPassword = password;
//        cognitoUser.getSessionInBackground(authenticationHandler);
//    }
    // Callback handler for the sign-in process
    AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
        @Override
        public void authenticationChallenge(ChallengeContinuation continuation) {

        }

        @Override
        public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
            Toast.makeText(appContext,"Sign in success", Toast.LENGTH_LONG).show();

        }

        @Override
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
            // The API needs user sign-in credentials to continue
            AuthenticationDetails authenticationDetails = new AuthenticationDetails(userId, userPassword, null);
            // Pass the user sign-in credentials to the continuation
            authenticationContinuation.setAuthenticationDetails(authenticationDetails);
            // Allow the sign-in to continue
            authenticationContinuation.continueTask();
        }

        @Override
        public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {
            // Multi-factor authentication is required; get the verification code from user
            //multiFactorAuthenticationContinuation.setMfaCode(mfaVerificationCode);
            // Allow the sign-in process to continue
            //multiFactorAuthenticationContinuation.continueTask();
        }

        @Override
        public void onFailure(Exception exception) {
            // Sign-in failed, check exception for the cause
            Toast.makeText(appContext,"Sign in Failure", Toast.LENGTH_LONG).show();
        }
    };

    public String getPoolID() {
        return poolID;
    }

    public void setPoolID(String poolID) {
        this.poolID = poolID;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public Regions getAwsRegion() {
        return awsRegion;
    }

    public void setAwsRegion(Regions awsRegion) {
        this.awsRegion = awsRegion;
    }

}
