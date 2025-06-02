package software.amazon.opentelemetry.android.demo.anr

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import software.amazon.opentelemetry.android.demo.anr.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var countDownTimer: CountDownTimer
    private val initialCountdown = ANRDemoApplication.DELAY_MILLISECONDS / 1000
    private val countDownInterval = 1000L // 1 second
    
    companion object {
        private const val TAG = "ANRDemoActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize the timer display
        binding.timerTextView.text = initialCountdown.toString()
        
        // Create and start the countdown timer
        startCountdownTimer()
        
        // Set up the ANR button
        binding.triggerAnrButton.setOnClickListener {
            Log.d(TAG, "ANR button clicked, blocking main thread")
            // Block the main thread to trigger ANR
            Thread.sleep(15000) // Sleep for 15 seconds to trigger ANR
            Log.d(TAG, "Main thread unblocked")
        }
    }
    
    private fun startCountdownTimer() {
        countDownTimer = object : CountDownTimer(initialCountdown * countDownInterval, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / countDownInterval
                binding.timerTextView.text = secondsRemaining.toString()
            }

            override fun onFinish() {
                binding.timerTextView.text = "0"
                binding.triggerAnrButton.visibility = View.VISIBLE
                Log.d(TAG, "Here")
                for(i in 1..10000000000000) {
                // Sleep for 15 seconds to trigger ANR
                }
                Log.d(TAG, "Timer finished, ANR button enabled")
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
