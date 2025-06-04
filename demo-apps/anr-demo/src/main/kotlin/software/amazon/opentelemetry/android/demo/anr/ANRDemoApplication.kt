package software.amazon.opentelemetry.android.demo.anr

import android.app.Application
import android.util.Log

class ANRDemoApplication : Application() {
    companion object {
        private const val TAG = "ANRDemoApplication"
        const val DELAY_MILLISECONDS = 10000L // 10 seconds
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "ANR Demo Application started")
    }
}
