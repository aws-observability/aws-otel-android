# ADOT Android UI Loading Instrumentation

The UI Loading module provides automatic instrumentation for measuring UI performance metrics, specifically Time-to-First-Draw (TTFD) for Activities and Fragments.

For more reading:
- https://developer.android.com/topic/performance/rendering/optimizing-view-hierarchies
- https://dev.to/pyricau/android-vitals-first-draw-time-m1d

## Telemetry

This instrumentation produces the following telemetry:

### Activity TimeToFirstDraw

* Type: Span
* Name: `TimeToFirstDraw`
* Description: The time measurement from Activity onCreate() to the first draw of the window [DecorView](https://developer.android.com/reference/android/view/Window#getDecorView())
* Attributes:
    * `screen.name`: name of the screen
    * `screen.view.nodes`: a recursive count of all View nodes under the window DecorView. This gives a measure of the amount of work being done in the *layout-and-measure* stage of the UI pipeline. A high count correlates with longer TTFD. System-defined layers are included in this count.
    * `screen.view.depth`: the depth of the deepest-nested View under the window DecorView. This gives a measure of your UI complexity; more work must be done for a deeply-nested View heirarchy. A high count correlates with longer TTFD. System-defined layers (usually 4-6) are included when calculating the depth.

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
