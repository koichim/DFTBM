package jp.ne.sppd.masuda.dftbm

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


/**
 * contextをどこからでも呼べるようにしたクラス
 * https://qiita.com/okb_m/items/6899c8dfaa8d784a76dc
 */
class SingletonContext : Application() {

    init {
        instance = this
    }

    companion object {
        private var instance: SingletonContext? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }

        fun getSharedPreference() : SharedPreferences {
            val applicationContext = applicationContext()
            return applicationContext.getSharedPreferences(applicationContext.getString(R.string.preference_key), Context.MODE_PRIVATE)
        }

        fun getDebugMode() : Boolean {
            val prefs = getSharedPreference()
            return prefs.getBoolean("debug", false)
        }

        @Throws(IOException::class)
        private fun execAndReadLines(command: String): Array<String> {
            val p = Runtime.getRuntime().exec(command)
            try {
                p.inputStream.use { `is` ->
                    InputStreamReader(`is`).use { isr ->
                        BufferedReader(isr).use { br ->
                            val list = ArrayList<Any>()
                            while (true) {
                                val s: String = br.readLine() ?: return list.toArray(arrayOfNulls<String>(0))
                                list.add(s)
                            }
                        }
                    }
                }
            } finally {
                p.destroy()
            }
            return arrayOf("execAndReadLines failed")
        }

        fun debugLsOf() {
            val ulimit = "ulimit -n"
            try {
                Log.d("SingletonContext:debugLsOf", "`" + ulimit + "` = " + execAndReadLines(ulimit)[0])
            } catch (e : IOException ) {
                Log.d("SingletonContext:debugLsOf", "failed to run: $ulimit", e)
            }
            val lsof = "lsof -p "+android.os.Process.myPid()
            try {
                val lines = execAndReadLines (lsof)
                Log.d(
                    "SingletonContext:debugLsOf",
                    "length of `$lsof` = ${lines.size}, number of active threads = ${Thread.activeCount()}"
                )
            } catch (e : Exception) {
                Log.d("SingletonContext:debugLsOf", "failed to run: $lsof", e)
            }
        }
    }

}
