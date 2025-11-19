package software.amazon.opentelemetry.android.demo.agent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.opentelemetry.api.trace.Span
import software.amazon.opentelemetry.android.OpenTelemetryRumClient
import software.amazon.opentelemetry.android.demo.agent.R
import software.amazon.opentelemetry.android.features.fragmentTTFDSpan

class InstrumentationTestFragment : Fragment() {
    private lateinit var ttfdSpan: Span

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ttfdSpan = OpenTelemetryRumClient.fragmentTTFDSpan("InstrumentationTestFragment")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_instrumentation_test, container, false)
        view.viewTreeObserver.addOnDrawListener {
            if (::ttfdSpan.isInitialized) {
                ttfdSpan.end()
            }
        }
        return view
    }

    companion object {
        fun newInstance() = InstrumentationTestFragment()
    }
}
