package jp.ne.sppd.masuda.dftbm

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import jp.ne.sppd.masuda.dftbm.DFTBMNotificator.Companion.createNotification
import java.lang.Thread.sleep

class DFTBMForegroundService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification("job", "started")

        Thread(
            Runnable {
                Log.i("DFTBMForegroundService", "thread start")
                val locator = DFTBMLocator(this)
                locator.makeLocationNotification()
            }).start()

        startForeground(2, notification)

        return START_STICKY
    }

    fun stopForegroundService(){
        stopForeground(STOP_FOREGROUND_DETACH)
    }
}
