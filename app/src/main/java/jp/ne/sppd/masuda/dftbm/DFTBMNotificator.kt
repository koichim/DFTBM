package jp.ne.sppd.masuda.dftbm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat


class DFTBMNotificator {

    companion object {
        private const val CHANNEL_ID = "DFTBM_chanel"
        private const val NAME = "DFTBM"
        private const val NOTIFY_DESCRIPTION = "Don't Forget to Buy Morning"

        fun sendNotification(title: String, text: String) {
            val applicationContext: Context = SingletonContext.applicationContext()
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            createNotificationChannel(manager)

            val notification : Notification = createNotification(title, text)

            manager.cancel(1)
            manager.notify(1, notification)
        }

        private fun createNotificationChannel(manager: NotificationManager) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                    val channel = NotificationChannel(CHANNEL_ID, NAME, NotificationManager.IMPORTANCE_HIGH)
                    channel.description = NOTIFY_DESCRIPTION
                    manager.createNotificationChannel(channel)
                }
            }
        }

        fun createNotification(title: String, text: String) : Notification {
            val applicationContext: Context = SingletonContext.applicationContext()

            return NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .apply {
                    setSmallIcon(R.drawable.ic_launcher_background)
                    setContentTitle(title)
                    setContentText(text)
                }.build()
        }

        fun cancelNotification(id: Int) {
            val applicationContext: Context = SingletonContext.applicationContext()
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            createNotificationChannel(manager)

            manager.cancel(id)
        }
    }
}

