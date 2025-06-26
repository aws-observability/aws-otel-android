# ADOT Android UI Loading Instrumentation

The UI Loading module provides automatic instrumentation for measuring UI performance metrics, specifically Time-to-First-Draw (TTFD) for Activities and Fragments.

## What it does

- **Time-to-First-Draw**: Measures when UI elements are first rendered
- **Activity Performance**: Automatic TTFD tracking for Activities
- **Fragment Performance**: Manual TTFD tracking for Fragments

## How it Works

The module uses Android's Activity lifecycle callbacks and ViewTreeObserver to detect when the first frame is drawn:

```
Activity Created → Register FirstDrawListener → First Draw Event → End TTFD Span
```

## Automatic Instrumentation

Activities are automatically instrumented when using the agent or core modules:

```kotlin
// No code required - automatic for Activities
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // TTFD span automatically created and ended
    }
}
```

## Manual Fragment Instrumentation

For Fragments, use the API module for manual instrumentation:

```kotlin
class MyFragment : Fragment() {
    private var ttfdSpan: Span? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        ttfdSpan = AwsRum.startFragmentTTFDSpan("MyFragment")
        return inflater.inflate(R.layout.fragment_my, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                ttfdSpan?.end()
            }
        })
    }
}
```

## Android Version Support

- **API 29+**: Uses `onActivityPreCreated` for precise timing
- **API 26-28**: Uses `onActivityCreated` with compatibility handling
