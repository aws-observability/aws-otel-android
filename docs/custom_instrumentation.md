# Custom Instrumentation

This guide shows you how to add custom telemetry to your Android app using the AWS Distro for OpenTelemetry.

## Custom Spans

Spans track operations in your app. Use them to measure how long something takes.

### Basic Span

```kotlin
import software.amazon.opentelemetry.android.OpenTelemetryRumClient
import software.amazon.opentelemetry.android.features.span

OpenTelemetryRumClient.span("load_user_profile") { span ->
    // Your code here - span automatically ends when block completes
    val user = database.getUser(userId)
    processUser(user)
}
```

### Span with Attributes

Add context to your spans with attributes:

```kotlin
import io.opentelemetry.api.common.Attributes

OpenTelemetryRumClient.span(
    name = "checkout",
    attributes = Attributes.builder()
        .put("cart.items", 3)
        .put("cart.total", 49.99)
        .put("payment.method", "credit_card")
        .build()
) { span ->
    processCheckout()
}
```

### Nested Spans

Track sub-operations within a larger operation:

```kotlin
OpenTelemetryRumClient.span("load_dashboard") { parentSpan ->
    loadUserData()

    OpenTelemetryRumClient.span("load_notifications") { childSpan ->
        fetchNotifications()
    }

    OpenTelemetryRumClient.span("load_feed") { childSpan ->
        fetchFeed()
    }
}
```

### Manual Span Control

If you need to start and end a span manually:

```kotlin
import io.opentelemetry.api.trace.Span

val tracer = OpenTelemetryRumClient.getInstance()?.openTelemetry?.getTracer("my-tracer")
val span = tracer?.spanBuilder("background_task")?.startSpan()

try {
    // Your work here
    doWork()
} finally {
    span?.end()
}
```

## Custom Events

Events are point-in-time occurrences. Use them to track user actions or important moments.

### Basic Event

```kotlin
import software.amazon.opentelemetry.android.features.event

OpenTelemetryRumClient.event("button_clicked")
```

### Event with Details

```kotlin
OpenTelemetryRumClient.event(
    eventName = "purchase_completed",
    body = "User completed checkout",
    attributes = Attributes.builder()
        .put("order.id", "12345")
        .put("order.total", 99.99)
        .build()
)
```

## Fragment Time-to-First-Draw

Track how long it takes for fragments to render:

```kotlin
import software.amazon.opentelemetry.android.features.fragmentTTFDSpan

class MyFragment : Fragment() {
    private var ttfdSpan: Span? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ttfdSpan = OpenTelemetryRumClient.fragmentTTFDSpan("MyFragment")
        return inflater.inflate(R.layout.fragment_my, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                view.viewTreeObserver.removeOnPreDrawListener(this)
                ttfdSpan?.end()
                return true
            }
        })
    }
}
```

## Common Patterns

### Tracking API Calls

```kotlin
OpenTelemetryRumClient.span("api_fetch_products") { span ->
    try {
        val response = apiClient.getProducts()
        span.setAttribute("response.status", response.code)
        span.setAttribute("response.items", response.items.size)
    } catch (e: Exception) {
        span.recordException(e)
        throw e
    }
}
```

### Tracking User Actions

```kotlin
binding.submitButton.setOnClickListener {
    OpenTelemetryRumClient.event(
        eventName = "form_submitted",
        attributes = Attributes.builder()
            .put("form.type", "registration")
            .put("form.fields", 5)
            .build()
    )
    submitForm()
}
```

### Tracking Background Work

```kotlin
lifecycleScope.launch {
    OpenTelemetryRumClient.span("sync_data") { span ->
        withContext(Dispatchers.IO) {
            val result = syncRepository.sync()
            span.setAttribute("sync.records", result.count)
        }
    }
}
```
