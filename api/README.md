# ADOT Android API

The ADOT Android API provides a simple, high-level interface for adding custom instrumentation to your Android application. It offers convenient methods for creating spans, tracking user interactions, and measuring performance without requiring deep OpenTelemetry knowledge.

## What it does

The API module enables you to:

- **Measure Performance**: Time critical code paths and business logic with custom spans
- **Add Context**: Attach custom attributes and screen information
- **Fragment TTFD**: Measure Time-to-First-Draw for fragments

## Quick Start

### 1. Add Dependency

```kotlin
dependencies {
    implementation("software.amazon.opentelemetry.android:agent:LATEST_VERSION") // automatically bundles the api module
}
```

### 2. Import and Use

```kotlin
import software.amazon.opentelemetry.android.api.AwsRum

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Simple span creation
        val span = AwsRum.startSpan("user_login")
        performLogin()
        span.end()
        
        // Or use executeSpan for automatic span lifecycle management
        AwsRum.executeSpan("load_user_data") { span ->
            loadUserData()
        }
    }
}
```

## Core API Methods

### Basic Span Creation

```kotlin
// Simple span
val span = AwsRum.startSpan("operation_name")
// ... do work
span.end()

// Span with screen context
val span = AwsRum.startSpan(
    name = "user_action",
    screenName = "MainActivity"
)
```

### Span with Attributes

```kotlin
val span = AwsRum.startSpan(
    name = "purchase_item",
    screenName = "CheckoutActivity",
    attributes = mapOf(
        "item_id" to "12345",
        "price" to 29.99,
        "quantity" to 2,
        "premium_user" to true
    )
)
```

### Execute Span (Recommended)

```kotlin
// Automatic span lifecycle management
AwsRum.executeSpan("database_query") { span ->
    // Span is automatically ended when block completes
    val result = database.query("SELECT * FROM users")
    span.setAttribute("result_count", result.size)
    return@executeSpan result
}
```

### Parent-Child Spans

```kotlin
AwsRum.executeSpan("parent_operation") { parentSpan ->
    // Parent operation work
    setupOperation()
    
    // Child span
    AwsRum.executeSpan(
        name = "child_operation",
        parent = parentSpan
    ) { childSpan ->
        performChildWork()
    }
    
    // More parent work
    cleanupOperation()
}
```

### Fragment Time-to-First-Draw

```kotlin
class MyFragment : Fragment() {
    private var ttfdSpan: Span? = null
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Start TTFD measurement
        ttfdSpan = AwsRum.startFragmentTTFDSpan("MyFragment")
        return inflater.inflate(R.layout.fragment_my, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // End TTFD when first draw is complete
        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                ttfdSpan?.end()
            }
        })
    }
}
```

### Advanced Span Options

```kotlin
// Span with explicit start time
val span = AwsRum.startSpan(
    name = "background_task",
    startTimeMs = System.currentTimeMillis() - 1000, // Started 1 second ago
    attributes = mapOf("task_type" to "data_sync")
)

// Child span with explicit start time
val childSpan = AwsRum.startChildSpan(
    name = "subtask",
    parent = parentSpan,
    startTimeMs = operationStartTime,
    spanKind = SpanKind.CONSUMER
)
```