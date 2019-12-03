package com.mapsrahal.maps.auth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

    private static SmsListener mListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data = intent.getExtras();

        Object[] pdus = (Object[]) data.get("pdus");

        for (int i = 0; i < pdus.length; i++) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);

            String sender = smsMessage.getDisplayOriginatingAddress();
            //Check the sender to filter messages which we require to read

            if (sender.contains("TFCTOR")) {

                String messageBody = smsMessage.getMessageBody();
                Log.d("message Recieved", messageBody);
                //Pass the message text to interface
                mListener.messageReceived(messageBody);
            }
        }
    }

    public static void bindListener(SmsListener listener) {
        mListener = listener;
    }
}
