package com.andreapivetta.changemywall.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class StartupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, StartupService.class));
    }
}
