package jp.ne.sppd.masuda.dftbm

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.util.Log

class DFTBMJobScheduler {
    companion object {
        private const val jobId = 1
        fun schedule(afterMilSec: Long) {
            val applicationContext: Context = SingletonContext.applicationContext()
            val scheduler: JobScheduler =
                applicationContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            val componentName =
                ComponentName(applicationContext, DFTBMJobService::class.java)

            val jobInfo = JobInfo.Builder(jobId, componentName)
                .apply {
                    setBackoffCriteria(10000, JobInfo.BACKOFF_POLICY_LINEAR)
                    setPersisted(false)
//                    setPeriodic(afterMilSec)
                    setMinimumLatency(afterMilSec)
                    setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    setRequiresCharging(false)
                }.build()

            scheduler.schedule(jobInfo)
            Log.i("DFTBMJobScheduler:schedule", "Job was scheduled")
        }

        fun reschedule(afterMilSec: Long) {
            val applicationContext: Context = SingletonContext.applicationContext()
            val scheduler: JobScheduler =
                applicationContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            val componentName =
                ComponentName(applicationContext, DFTBMJobService::class.java)

            val jobInfo = JobInfo.Builder(jobId, componentName)
                .apply {
                    setMinimumLatency(afterMilSec)
                    setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                }.build()

            scheduler.schedule(jobInfo)
            Log.i("DFTBMJobScheduler:reschedule", "Job was rescheduled")
        }

        fun cancel() {
            val applicationContext: Context = SingletonContext.applicationContext()
            val scheduler: JobScheduler =
                applicationContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

            scheduler.cancelAll()
            Log.i("DFTBMJobScheduler:cancel", "Job was canceled")
        }
    }
}