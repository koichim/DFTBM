package jp.ne.sppd.masuda.dftbm

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.util.Log
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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
//                    setPeriodic(afterMilSec)      // Periodic or MinimumLatency
                    setMinimumLatency(afterMilSec)
                    setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                    setRequiresCharging(false)
                }.build()

            scheduler.schedule(jobInfo)
            Log.i("DFTBMJobScheduler:schedule", "Job was scheduled after $afterMilSec ms")
        }

        fun scheduleNext15() {
            val now = LocalDateTime.now()
            val next15 = LocalDateTime.now().plusDays(1).withHour(15).withMinute(0).withSecond(0)
            val afterMilSec = ChronoUnit.SECONDS.between(now, next15) * 1000

            schedule(afterMilSec)

            Log.i("DFTBMJobScheduler:scheduleNext15", "Job was scheduled at "+next15.format(DateTimeFormatter.ofPattern("MM/dd HH:mm:ss")))
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