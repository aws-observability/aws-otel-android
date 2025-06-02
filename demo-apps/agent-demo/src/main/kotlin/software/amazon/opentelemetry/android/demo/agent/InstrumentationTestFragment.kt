package software.amazon.opentelemetry.android.demo.agent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import software.amazon.opentelemetry.android.demo.agent.R

class InstrumentationTestFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_instrumentation_test, container, false)
    }

    companion object {
        fun newInstance() = InstrumentationTestFragment()
    }
}
