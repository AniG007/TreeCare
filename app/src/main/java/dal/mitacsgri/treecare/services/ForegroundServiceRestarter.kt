package dal.mitacsgri.treecare.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class ForegroundServiceRestarter : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("Broadcast Received", "Service was stopped")

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            context?.startForegroundService(Intent(context, ForegroundService::class.java))
        }
        else{
            context?.startService(Intent(context, ForegroundService::class.java))
        }
    }
}