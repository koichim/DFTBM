package jp.ne.sppd.masuda.dftbm

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.util.Log

class DFTBMJobService  : JobService() {
    override fun onStopJob(params: JobParameters?): Boolean {

        jobFinished(params, false)
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        val applicationContext: Context = SingletonContext.applicationContext()
        val serviceIntent = Intent(applicationContext, DFTBMForegroundService::class.java)

        startForegroundService(serviceIntent)
        Log.i("DFTBMJobService:onStartJob", "ForegroundService was started")

        jobFinished(params, false)
        return true
    }
}
