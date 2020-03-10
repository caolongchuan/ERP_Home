package com.clc.test_web.JPush;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.clc.test_web.BrowserActivity;

import cn.jpush.android.api.NotificationMessage;
import cn.jpush.android.service.JPushMessageReceiver;

public class JPushReceiver extends JPushMessageReceiver {

    @Override
    public void onNotifyMessageOpened(Context context, NotificationMessage notificationMessage) {
        super.onNotifyMessageOpened(context, notificationMessage);

        Intent intent1 = new Intent();
        intent1.setAction("closs");
        context.sendBroadcast(intent1);

        Intent intent = new Intent(context, BrowserActivity.class);
        if (!(context instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }
}
