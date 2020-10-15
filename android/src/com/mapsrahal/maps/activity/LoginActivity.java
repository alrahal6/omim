package com.mapsrahal.maps.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.mapsrahal.maps.MwmApplication;
import com.mapsrahal.maps.MySharedPreference;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.activity.ui.BlockActivity;
import com.mapsrahal.maps.api.ApiClient;
import com.mapsrahal.maps.api.ApiInterface;
import com.mapsrahal.maps.api.TokenApi;
import com.mapsrahal.maps.auth.IsBlocked;
import com.mapsrahal.maps.auth.MessageResponse;
import com.mapsrahal.maps.model.AmIBlocked;
import com.mapsrahal.maps.model.NewToken;
import com.mapsrahal.maps.model.User;
import com.mapsrahal.util.UiUtils;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private Button btnSubmit,mOtpSubmit;
    private EditText name, mobileNumber, etOTP;
    private LinearLayout mOtpTab,mNamePhone;
    private ProgressBar mProgressBar,mOtpProgress;
    private ApiInterface apiService;
    private TokenApi tokenApi;
    private String mUserName,mMobile,mPassword,otpAuto;
    public static final String OTP_REGEX = "[0-9]{1,6}";
    private static final long START_TIME_IN_MILLIS = 100000;
    private TextView mTextViewCountDown;
    private CountDownTimer mCountDownTimer;

    private boolean mTimerRunning;

    private long mTimeLeftInMillis = START_TIME_IN_MILLIS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiUtils.setupColorStatusBar(this, R.color.bg_statusbar);
        //Log.i("id token", "here");
        apiService = ApiClient.getClient().create(ApiInterface.class);
        tokenApi = ApiClient.getClient().create(TokenApi.class);
        if(MySharedPreference.getInstance(getApplicationContext()).isLoggedIn()) {
            //startActivity();
            sendTokenToServer();
            startActivity();
            //return;
            //MySharedPreference.getInstance(this).setCaptainOnline(false);
            //startActivity(new Intent(this, SelectorActivity.class));
            //finish();
            //return;
        } else {
            setContentView(R.layout.activity_login);
            mTextViewCountDown = findViewById(R.id.text_view_countdown);
            mNamePhone = findViewById(R.id.name_phone);
            btnSubmit = findViewById(R.id.btnSubmit);
            name = findViewById(R.id.name);
            //email = findViewById(R.id.email);
            mobileNumber = findViewById(R.id.mobileNumber);
            mOtpTab = findViewById(R.id.otp_tab);
            etOTP = findViewById(R.id.etOTP);
            mOtpSubmit = findViewById(R.id.btnOtp);
            mOtpSubmit.setOnClickListener(this);
            mProgressBar = findViewById(R.id.loginProgressOtp);
            mOtpProgress = findViewById(R.id.otpVerifyProgress);
            mProgressBar.setVisibility(View.GONE);
            btnSubmit.setOnClickListener(this);
        }
    }

    private void sendTokenToServer() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        int id = MySharedPreference.getInstance(getApplicationContext()).getUserId();
                        Log.d("id token", ""+id +" "+ token);
                        NewToken newToken = new NewToken(id, token);
                        Call<NewToken> call = tokenApi.sendToken(newToken);
                        call.enqueue(new Callback<NewToken>() {
                            @Override
                            public void onResponse(Call<NewToken> call, Response<NewToken> response) {
                                //MySharedPreference.getInstance(getApplicationContext()).clearNewToken();
                            }

                            @Override
                            public void onFailure(Call<NewToken> call, Throwable t) {

                            }
                        });
                    }
                });
    }
    
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSubmit:
                if ((mobileNumber.getText().length() < 9) ||
                        TextUtils.isEmpty(name.getText().toString())) {

                    DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                dialog.dismiss();
                                break;
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage("Enter all the information requested").setPositiveButton("Dismiss", dialogClickListener)
                            .show();
                } else {
                    userLogin();
                }
                break;

            case R.id.btnOtp:
                if ((mobileNumber.getText().length() < 9) ||
                        TextUtils.isEmpty(name.getText().toString()) || etOTP.getText().length() < 6) {

                    DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                dialog.dismiss();
                                break;
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage("Enter valid otp").setPositiveButton("Dismiss", dialogClickListener)
                            .show();
                } else {
                    mOtpSubmit.setVisibility(View.GONE);
                    mOtpProgress.setVisibility(View.VISIBLE);
                    checkOtp();
                }
                break;
        }

    }

    private void userLogin() {
        // todo check OTP from server
        //phone = etPhone.getText().toString().trim();

        mMobile = mobileNumber.getText().toString().trim();
        mUserName = name.getText().toString().trim();
        //password = etOTPs.getText().toString().trim();
        //mProgressBar.setVisibility(View.VISIBLE);
        // Check if no view has focus:
        mProgressBar.setVisibility(View.VISIBLE);
        btnSubmit.setVisibility(View.GONE);
        User user = new User(mUserName,mMobile,1,"");
        // todo
        Call<User> call = apiService.sentOTP(user);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                /*int id = response.body().getId();
                MySharedPreference.getInstance(getApplicationContext())
                        .userLogin(id, mMobile, mUserName,1);
                sendTokenToServer();
                startActivity(new Intent(getApplicationContext(), SelectorActivity.class));
                finish();
                return;*/
                mOtpTab.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                mNamePhone.setVisibility(View.GONE);
                startTimer();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d("Login Error!", ""+t.getMessage());
            }
        });
    }

    private void checkOtp() {
        //Log.d(TAG, "Check otp");
        //ApiInterface apiService =
                //ApiClient.getClient().create(ApiInterface.class);
        mMobile = mobileNumber.getText().toString().trim();
        mUserName = name.getText().toString().trim();
        otpAuto = etOTP.getText().toString().trim();
        mProgressBar.setVisibility(View.VISIBLE);
        //Log.d(TAG, "Mobile : "+mMobile);
        //Log.d(TAG, "Name : "+mUserName);
        //Log.d(TAG, "otp : "+ otpAuto);
        User user = new User(mUserName,mMobile,1,otpAuto);
        Call<User> call = apiService.verifyOTP(user);
        //Log.d(TAG, "Check otp inside");
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                //Log.d(TAG, "Check otp response");
                if (response.isSuccessful()) {
                    int id = response.body().getId();
                    MySharedPreference.getInstance(getApplicationContext())
                            .userLogin(id, mMobile, mUserName,1);
                    sendTokenToServer();
                    startActivity(new Intent(getApplicationContext(), SelectorActivity.class));
                    finish();
                    return;
                        /*MySharedPreference.getInstance(getApplicationContext())
                                .userLogin(1, mMobile, mUserName,1);
                        startActivity(new Intent(getApplicationContext(), SelectorActivity.class));
                        finish();
                        return;*/
                    } else {
                        Toast.makeText(LoginActivity.this,"Please check your OTP",Toast.LENGTH_LONG).show();
                        revertBack();
                        //Log.d(TAG, "Error in Login");
                    }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                //Log.e(TAG, t.toString());
                mProgressBar.setVisibility(View.GONE);
                revertBack();
                Toast.makeText(LoginActivity.this,"Please check your OTP",Toast.LENGTH_LONG).show();
            }
        });
        /*call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.d(TAG, "Check otp response");
                    if (response.isSuccessful()) {
                        int id = response.body().getId();
                        MySharedPreference.getInstance(getApplicationContext())
                                .userLogin(id, mMobile, mUserName,1);
                        sendTokenToServer();
                        startActivity(new Intent(getApplicationContext(), SelectorActivity.class));
                        finish();
                        return;
                        /*MySharedPreference.getInstance(getApplicationContext())
                                .userLogin(1, mMobile, mUserName,1);
                        startActivity(new Intent(getApplicationContext(), SelectorActivity.class));
                        finish();
                        return;*/ /*
                    } else {
                        Log.d(TAG, "Error in Login");
                    }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, t.toString());
            }

        });*/
    }

    private void revertBack() {
        etOTP.setText("");
        mOtpProgress.setVisibility(View.GONE);
        mOtpSubmit.setVisibility(View.VISIBLE);
    }

    private void startTimer() {
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                hideOtp();
                etOTP.setText("");
                mTimeLeftInMillis = START_TIME_IN_MILLIS;
                mNamePhone.setVisibility(View.VISIBLE);
                mOtpProgress.setVisibility(View.GONE);
                mOtpSubmit.setVisibility(View.VISIBLE);
            }
        }.start();

        mTimerRunning = true;
    }

    private void hideOtp() {
        mOtpTab.setVisibility(View.GONE);
        btnSubmit.setVisibility(View.VISIBLE);
    }

    private void updateCountDownText() {
        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        mTextViewCountDown.setText(timeLeftFormatted);
    }

    private void startActivity() {
        int id = MySharedPreference.getInstance(getApplicationContext()).getUserId();
        String phone = MySharedPreference.getInstance(getApplicationContext()).getPhoneNumber();
        AmIBlocked isBlocked = new AmIBlocked(id,phone,10);
        Call<IsBlocked> call = apiService.verifyUser(isBlocked);
        call.enqueue(new Callback<IsBlocked>() {
            @Override
            public void onResponse(Call<IsBlocked> call, Response<IsBlocked> response) {
                if(!response.isSuccessful()) {
                    //doNotAllow();
                    //return;
                    checkInternet();
                }
                if(response.body().isAllowed()) {
                    allowIn();
                } else {
                    doNotAllow();
                }
            }

            @Override
            public void onFailure(Call<IsBlocked> call, Throwable t) {
                //doNotAllow();
                //return;
                checkInternet();
            }
        });
    }

    private void allowIn() {
        sendTokenToServer();
        startActivity(new Intent(this, SelectorActivity.class));
        finish();
        return;
    }

    private void doNotAllow() {
        Intent intent = new Intent(this, BlockActivity.class);
        startActivity(intent);
    }

    private void checkInternet() {
        Intent intent = new Intent(this, CheckInternet.class);
        startActivity(intent);
    }

}
