package jp.ne.sppd.masuda.dftbm

import android.content.Context
import android.location.Location
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DFTBMLocator(theService: DFTBMForegroundService) {

    // 位置情報を取得できるクラス
    private var parentService : DFTBMForegroundService = theService

    private class DFTBMLocationCallback(theService: DFTBMForegroundService, theFusedLocationClient: FusedLocationProviderClient) : LocationCallback() {
        private var parentService : DFTBMForegroundService = theService
        private var fusedLocationClient: FusedLocationProviderClient = theFusedLocationClient
        override fun onLocationResult(locationResult: LocationResult?) {
            val today = LocalDate.now()
            val now = LocalTime.now()
            val nowDateStr: String = today.format(DateTimeFormatter.ofPattern("M月d日(E)"))
            val nowTimeStr: String = now.format(DateTimeFormatter.ofPattern("H時m分s秒"))

            val notificationTitle = "job at $nowDateStr$nowTimeStr"

            // 更新直後の位置が格納されているはず
            val location = locationResult?.lastLocation ?: return

            fusedLocationClient.removeLocationUpdates(this) // stop the location update

            val nanosPassed = SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos
            val locationTime : LocalDateTime = LocalDateTime.now().minusNanos(nanosPassed)
            val locationTimeStr: String = locationTime.format(DateTimeFormatter.ofPattern("H時m分s秒"))


            // put calculation of next happening
            // 緯度(latitude), 経度(longitude)
            // threshold for home: 35.612938,  139.744958
            // 天妙国寺ポイント：  35.6119118, 139.7434601
            // 天妙国寺から8km圏内：30min
            // 天妙国寺から4km圏内：15min
            // 天妙国寺から2km圏内：6min
            // 天妙国寺から1km圏内：3min
            val thePoint = Location(location)
            thePoint.latitude = 35.6119118
            thePoint.longitude = 139.744958
            val distance: Float = location.distanceTo(thePoint)
            val sleepMin: Int = when {
                4000 < distance  -> 30
                2000 < distance && distance <= 4000 -> 15
                1000 < distance && distance <= 2000 -> 6
                15 <= now.hour && 139.744958 < location.longitude -> (24 - now.hour + 15) * 60 // 明日の15時過ぎ
                else -> 3
            }
            val sleepText = "sleep "+sleepMin.toString()+"分"

            if (30 < sleepMin) {
                DFTBMJobScheduler.schedule(sleepMin.toLong() * 60 * 1000)
                parentService.stopForegroundService()
                DFTBMNotificator.cancelNotification(2) // remove foreground service notification
            } else {
                val notificationText =
                    "(" + location.latitude + ", " + location.longitude + ") at " + locationTimeStr + " " + sleepText
                Log.i("DFTBMLocator:DFTBMLocationCallback", notificationText)
                val notification = DFTBMNotificator.createNotification(notificationTitle, notificationText)
                parentService.startForeground(2, notification)
                DFTBMJobScheduler.schedule(sleepMin.toLong() * 60 * 1000)
                parentService.stopForegroundService()
            }


        }
    }

    fun makeLocationNotification() {
        Log.i("DFTBMLocator","makeLocationNotification() enter")
        val applicationContext: Context = SingletonContext.applicationContext()
        var fusedLocationClient = FusedLocationProviderClient(applicationContext)

        // どのような取得方法を要求
        val locationRequest = LocationRequest().apply {
            // 精度重視(電力大)と省電力重視(精度低)を両立するため2種類の更新間隔を指定
            // 今回は公式のサンプル通りにする。
            interval = 5000                                   // 最遅の更新間隔(但し正確ではない。)
            fastestInterval = 3000                             // 最短の更新間隔
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY  // 精度重視
            setExpirationDuration(1800000)                     // 30 min
        }

        // コールバック
        val locationCallback = DFTBMLocationCallback(this.parentService, fusedLocationClient)

        // 位置情報を更新
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }
}