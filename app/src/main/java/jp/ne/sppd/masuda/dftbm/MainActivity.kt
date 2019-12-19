package jp.ne.sppd.masuda.dftbm

import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.util.Log
import android.widget.Switch
import android.widget.ToggleButton


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

        // Debug switch
        if (SingletonContext.getDebugMode()) debugSwitch.isChecked = true

        val prefs = getSharedPreferences(getString(R.string.preference_key), android.content.Context.MODE_PRIVATE)
        debugSwitch.setOnCheckedChangeListener { _, isChecked ->
            val editor = prefs.edit()
            when {
                isChecked -> editor.putBoolean("debug", true)
                else      -> editor.putBoolean("debug",false)
            }
            editor.apply()
            val debugMode = prefs.getBoolean("debug", false)
            Log.i("MainActivity", "debug $debugMode")
        }
    }

}

