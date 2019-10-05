package jp.ne.sppd.masuda.dftbm

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class DFTBMForegroundService : Service() {
    private val locator = DFTBMLocator(this)

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = DFTBMNotificator.createNotification("job", "started")

        Thread(
            Runnable {
                Log.i("DFTBMForegroundService", "thread start")
                locator.makeLocationNotification(0)
                Log.i("DFTBMForegroundService", "thread end")
            }).start()

        startForeground(2, notification)

        return START_STICKY
    }

    fun updateNotification(notification: Notification) {
        Log.i("DFTBMForegroundService:updateNotification", "updateNotification enter")
        startForeground(2, notification)
    }

    fun stopForegroundService(){
        Log.i("DFTBMForegroundService:stopForegroundService", "stopForegroundService enter")
        stopForeground(STOP_FOREGROUND_REMOVE)
//        DFTBMNotificator.cancelNotification(2) // remove foreground service notification
    }

    override fun onDestroy() {
        Log.i("DFTBMForegroundService:onDestroy", "Destroy. Cleanup foreground service")
        locator.stopLocationNotification()
        stopForegroundService()
        super.onDestroy()
    }
}
