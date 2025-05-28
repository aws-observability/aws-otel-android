package software.amazon.opentelemetry.android.demo.crash

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import software.amazon.opentelemetry.android.demo.crash.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var countDownTimer: CountDownTimer
    private val initialCountdown = 120 // 120 seconds
    private val countDownInterval = 1000L // 1 second
    
    companion object {
        private const val TAG = "CrashDemoActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize the timer display
        binding.timerTextView.text = initialCountdown.toString()
        
        // Create and start the countdown timer
        startCountdownTimer()
    }
    
    private fun startCountdownTimer() {
        countDownTimer = object : CountDownTimer(initialCountdown * countDownInterval, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / countDownInterval
                binding.timerTextView.text = secondsRemaining.toString()
                Log.d(TAG, "Timer: $secondsRemaining seconds remaining")
            }

            override fun onFinish() {
                binding.timerTextView.text = "0"
                Log.d(TAG, "Timer finished")
            }
        }
        
        // Start the timer
        countDownTimer.start()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Cancel the timer to prevent memory leaks
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }
}
