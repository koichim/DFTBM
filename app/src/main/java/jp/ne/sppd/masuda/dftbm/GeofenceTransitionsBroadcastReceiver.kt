package jp.ne.sppd.masuda.dftbm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class GeofenceTransitionsBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return

        val debugMode = SingletonContext.getDebugMode()

//        val date_str: String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val dateStr: String = LocalDate.now().format(DateTimeFormatter.ofPattern("M月d日(E)"))
        val timeStr: String = LocalTime.now().format(DateTimeFormatter.ofPattern("H時m分"))

        var notify : Boolean = when {
            debugMode -> true
            else      -> false
        }
        var notificationText = "Enter"
        if (LocalDate.now().dayOfWeek == DayOfWeek.THURSDAY && 15 < LocalTime.now().hour) {
            notificationText = "モーニング買って"
            notify = true
        }
        if (debugMode) notificationText += " at $dateStr$timeStr"

        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (notify) {
            when (geofencingEvent.geofenceTransition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> DFTBMNotificator.sendNotification("",notificationText)
                Geofence.GEOFENCE_TRANSITION_EXIT -> DFTBMNotificator.sendNotification("", "Exit")
                else -> DFTBMNotificator.sendNotification("", "error")
            }
        }
    }
}