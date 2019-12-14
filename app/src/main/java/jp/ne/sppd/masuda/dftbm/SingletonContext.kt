package jp.ne.sppd.masuda.dftbm

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

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
    }
}
