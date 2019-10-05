package jp.ne.sppd.masuda.dftbm

import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
            }
        }

        addButton.setOnClickListener {
            DFTBMGeofencer.addTheGeofence()
            DFTBMNotificator.sendNotification("", "added")
        }

        startJobButton.setOnClickListener {
            DFTBMJobScheduler.schedule(5000)
        }

        cancelJobButton.setOnClickListener {
            DFTBMJobScheduler.cancel()
            val intent = Intent(this@MainActivity, DFTBMForegroundService::class.java)
            stopService(intent)
        }

    }
}
