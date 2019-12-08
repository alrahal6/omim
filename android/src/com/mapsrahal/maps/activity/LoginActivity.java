package com.mapsrahal.maps.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.mapsrahal.maps.MySharedPreference;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.api.ApiClient;
import com.mapsrahal.maps.api.ApiInterface;
import com.mapsrahal.maps.api.TokenApi;
import com.mapsrahal.maps.model.NewToken;
import com.mapsrahal.maps.model.User;
import com.mapsrahal.util.UiUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private Button btnSubmit,mOtpSubmit;
    private EditText name, mobileNumber, etOTP;
    private LinearLayout mOtpTab;
    private ProgressBar mProgressBar;
    private ApiInterface apiService;
    private TokenApi tokenApi;
    private String mUserName,mMobile,mPassword,otpAuto;
    public static final String OTP_REGEX = "[0-9]{1,6}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiUtils.setupColorStatusBar(this, R.color.bg_statusbar);
        setContentView(R.layout.activity_login);
        //Log.i("id token", "here");
        apiService = ApiClient.getClient().create(ApiInterface.class);
        tokenApi = ApiClient.getClient().create(TokenApi.class);
        if(MySharedPreference.getInstance(getApplicationContext()).isLoggedIn()) {
            sendTokenToServer();
            startActivity(new Intent(this, SelectorActivity.class));
            finish();
            return;
        }
        btnSubmit = findViewById(R.id.btnSubmit);
        name = findViewById(R.id.name);
        //email = findViewById(R.id.email);
        mobileNumber = findViewById(R.id.mobileNumber);
        mOtpTab = findViewById(R.id.otp_tab);
        etOTP = findViewById(R.id.etOTP);
        mOtpSubmit = findViewById(R.id.btnOtp);
        mOtpSubmit.setOnClickListener(this);
        mProgressBar = findViewById(R.id.loginProgressOtp);
        mProgressBar.setVisibility(View.GONE);
        btnSubmit.setOnClickListener(this);

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
                        //Log.d("id token", ""+id +" "+ token);
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
                        // Log and toast
                        //String msg = getString(R.string.msg_token_fmt, token);
                        //Log.d(TAG, msg);
                        //Toast.makeText(org.alohalytics.demoapp.MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
        /*if(MySharedPreference.getInstance(getApplicationContext()).isHaveNewToken()) {
        //Log.i("id token", "yesssssss");
            int id = MySharedPreference.getInstance(getApplicationContext()).getUserId();
            String token = FirebaseInstanceId.getInstance().getToken();
            NewToken newToken = new NewToken(id, token);
            Log.i("id token",newToken+"");
            Call<NewToken> call = tokenApi.sendToken(newToken);
            call.enqueue(new Callback<NewToken>() {
                @Override
                public void onResponse(Call<NewToken> call, Response<NewToken> response) {
                    MySharedPreference.getInstance(getApplicationContext()).clearNewToken();
                }

                @Override
                public void onFailure(Call<NewToken> call, Throwable t) {

                }
            });
        //Log.i("id token", ""+id +" "+ token);
        }*/
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
                checkOtp();
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
        User user = new User(mUserName,mMobile,1);
        // todo
        //mOtpTab.setVisibility(View.VISIBLE);
        Call<User> call = apiService.sentOTP(user);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                int id = response.body().getId();
                MySharedPreference.getInstance(getApplicationContext())
                        .userLogin(id, mMobile, mUserName,1);
                sendTokenToServer();
                startActivity(new Intent(getApplicationContext(), SelectorActivity.class));
                finish();
                return;
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d("Details User", ""+t.getMessage());
            }
        });
        /*call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                String err = response.body().getStatus();
                String dtls = response.body().getDetails();
                Log.d("Details User", err);
                Log.d("Details User", dtls);
                //getOTP();
                mOtpTab.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                mOtpTab.setVisibility(View.GONE);
                btnSubmit.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                Log.e("ERROR", t.toString());
            }

        });*/
    }

    private void checkOtp() {
        /*ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        otpAuto = etOTP.getText().toString().trim();
        Call<MessageResponse> call = apiService.verifyOTP(otpAuto);
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                try {
                    if (response.body().getStatus().equals("Success")) {
                        MySharedPreference.getInstance(getApplicationContext())
                                .userLogin(1, mMobile, mUserName,1);
                        startActivity(new Intent(getApplicationContext(), SelectorActivity.class));
                        finish();
                        return;
                    } else {
                        Log.d("Failure", response.body().getDetails() + "|||" + response.body().getStatus());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Log.e("ERROR", t.toString());
            }

        });*/
    }


}
