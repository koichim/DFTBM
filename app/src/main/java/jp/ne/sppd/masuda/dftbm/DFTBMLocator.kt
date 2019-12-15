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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DFTBMLocator(theService: DFTBMForegroundService) {

    // 位置情報を取得できるクラス
    private var parentService : DFTBMForegroundService = theService
    private val applicationContext: Context = SingletonContext.applicationContext()
    private var fusedLocationClient = FusedLocationProviderClient(applicationContext)
    private val locationCallback = DFTBMLocationCallback()

    private inner class DFTBMLocationCallback : LocationCallback() {
        private var prevSleepMin = 0
        override fun onLocationResult(locationResult: LocationResult?) {

            if (SingletonContext.getDebugMode()) SingletonContext.debugLsOf()

            val today = LocalDate.now()
            val now = LocalTime.now()
            val nowDateStr: String = today.format(DateTimeFormatter.ofPattern("M月d日(E)"))
            val nowTimeStr: String = now.format(DateTimeFormatter.ofPattern("H時m分s秒"))

            val notificationTitle = "job at $nowDateStr$nowTimeStr"

            // 更新直後の位置が格納されているはず
            val location = locationResult?.lastLocation ?: return

            val nanosPassed = SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos
            val locationTime : LocalDateTime = LocalDateTime.now().minusNanos(nanosPassed)
            val locationTimeStr: String = locationTime.format(DateTimeFormatter.ofPattern("H時m分s秒"))

            // put calculation of next happening
            // 緯度(latitude), 経度(longitude)
            // threshold for home: 35.611934, 139.745178
            // 天妙国寺ポイント：  35.611934, 139.743638
            // 天妙国寺から8.15km圏内：20min
            // 天妙国寺から4km圏内：15min
            // 天妙国寺から2km圏内：6min
            // 天妙国寺から1km圏内：3min
            // (その他)
            // 15時前の場合は15時過ぎにセットし直し
            // !debuｇ && 木曜日じゃない場合、明日の15時過ぎ
            // 天妙国寺から30km異常離れている場合、この日は無視
            // 経度が139.745178より大きい場合は、家に帰ってると判断
            val thePoint = Location(location)
            thePoint.latitude = 35.611934
            thePoint.longitude = 139.743638
            val distance: Float = location.distanceTo(thePoint)
            val sleepMin: Int = when {
                now.hour < 15                       -> (15 - now.hour) * 60 // 今日の15時過ぎ
                !SingletonContext.getDebugMode() && LocalDate.now().dayOfWeek != DayOfWeek.THURSDAY ->  (24 - now.hour + 15) * 60 // 明日の15時過ぎ
                30000 < distance                    -> (24 - now.hour + 15) * 60 // 明日の15時過ぎ
                8150 < distance                     -> 30 // 会社出て川渡る前
                4000 < distance && distance <= 8150 -> 20 // 会社出て川渡った後～田町駅
                2000 < distance && distance <= 4000 -> 15 // 田町駅～品川駅
                1000 < distance && distance <= 2000 -> 6  // 品川駅～品川学園付近
                139.745178 < location.longitude     -> (24 - now.hour + 15) * 60 // 明日の15時過ぎ
                else                                -> 3  //品川学園より手前
            }
            val sleepText = "sleep "+sleepMin.toString()+"分"

            if (30 < sleepMin) {
                // complete foreground service
                Log.i("DFTBMLocator:DFTBMLocationCallback", "location job done")
                fusedLocationClient.removeLocationUpdates(this) // stop the location update
                parentService.stopForegroundService()
            } else {

//                val notificationText = distance.toInt().toString() + " m from 天妙国寺 at $locationTimeStr, $sleepText"
                val notificationText = "${distance.toInt()} m from 天妙国寺 ($sleepText) at $locationTimeStr"
                Log.i("DFTBMLocator:DFTBMLocationCallback", notificationText)

                if (SingletonContext.getDebugMode()) {
                    val notification =
                        DFTBMNotificator.createNotification(notificationTitle, notificationText)
                    parentService.updateNotification(notification) // modify notification
                }

                if (prevSleepMin != sleepMin) {
                    fusedLocationClient.removeLocationUpdates(this) // stop the location update
                    this@DFTBMLocator.makeLocationNotification(sleepMin.toLong() * 60 * 1000) // schedule next location
                    prevSleepMin = sleepMin
                }
            }
        }
    }

    fun makeLocationNotification(nextMilSec: Long) {
        Log.i("DFTBMLocator:makeLocationNotification","makeLocationNotification($nextMilSec) enter")
        val waitMilSec = if (nextMilSec==0L) 60000 else nextMilSec
        // どのような取得方法を要求
        val locationRequest = LocationRequest().apply {
            // 精度重視(電力大)と省電力重視(精度低)を両立するため2種類の更新間隔を指定
            // 今回は公式のサンプル通りにする。
            interval = waitMilSec                              // 最遅の更新間隔(但し正確ではない。)
            maxWaitTime = waitMilSec                           // start after (maybe)
            fastestInterval = waitMilSec                       // 最短の更新間隔
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY  // 精度重視
        }

        // 位置情報を更新
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun stopLocationNotification() {
        Log.i("DFTBMLocator:stopLocationNotification","stopLocationNotification enter")
        fusedLocationClient.removeLocationUpdates(locationCallback) // stop the location update
    }
}