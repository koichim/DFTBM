package jp.ne.sppd.masuda.dftbm

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.GeofencingClient


class DFTBMGeofencer {

    companion object {
        fun addTheGeofence() {
            val applicationContext: Context = SingletonContext.applicationContext()

            val geofencingClient: GeofencingClient =
                LocationServices.getGeofencingClient(applicationContext)

            val geofence = Geofence.Builder()
                .setRequestId("Geofence")
                .setCircularRegion(35.611934, 139.743638, 100f)     // 天妙国寺脇から半径100m
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER /* or Geofence.GEOFENCE_TRANSITION_EXIT */)
                .build()

            val request = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

//            val intent = Intent("jp.ne.sppd.masuda.dftbm.ACTION_RECEIVE_GEOFENCE")
            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                0,
                Intent(applicationContext, GeofenceTransitionsBroadcastReceiver::class.java),
//                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Geofenceの設置
            geofencingClient.addGeofences(request, pendingIntent)?.also {
                it.addOnSuccessListener {
                    Toast.makeText(
                        applicationContext,
                        "addOnSuccess",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                it.addOnFailureListener {
                    Toast.makeText(
                        applicationContext,
                        "addOnFailure",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

}