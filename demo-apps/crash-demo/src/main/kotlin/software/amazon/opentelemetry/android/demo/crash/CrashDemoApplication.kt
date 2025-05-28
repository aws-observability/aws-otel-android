package software.amazon.opentelemetry.android.demo.crash

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log

class CrashDemoApplication : Application() {
    companion object {
        private const val TAG = "CrashDemoApplication"
        private const val DELAY_MILLISECONDS = 120000L // 120 seconds
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Crash Demo Application started")

        // Create a handler associated with the main looper
        Handler(Looper.getMainLooper()).postDelayed({
            throw Exception("Testing exception")
        }, DELAY_MILLISECONDS)
    }
}
