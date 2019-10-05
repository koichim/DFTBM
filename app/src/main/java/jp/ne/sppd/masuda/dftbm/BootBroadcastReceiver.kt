package jp.ne.sppd.masuda.dftbm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        DFTBMGeofencer.addTheGeofence()
        DFTBMNotificator.sendNotification("", "Geofence was added at boot")
        DFTBMJobScheduler.schedule(180000)
    }
}