package com.coretal.carinspection.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.coretal.carinspection.services.SyncService;
import com.coretal.carinspection.utils.MyHelper;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        Log.d("Kangtle", "Starting the Sync service");
//////        if(!SyncService.isRunning){
//////            Intent syncServiceIntent = new Intent(context, SyncService.class);
//////            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//////                context.startForegroundService(syncServiceIntent);
//////            }else{
//////                context.startService(syncServiceIntent);
//////            }
//////        }
    }
}
