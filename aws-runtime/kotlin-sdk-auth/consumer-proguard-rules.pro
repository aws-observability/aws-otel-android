# Keep specific classes in the kotlin auth package
-keep class software.amazon.opentelemetry.android.auth.kotlin.export.AwsSigV4LogRecordExporter { *; }
-keep class software.amazon.opentelemetry.android.auth.kotlin.export.AwsSigV4LogRecordExporterBuilder { *; }
-keep class software.amazon.opentelemetry.android.auth.kotlin.export.AwsSigV4SpanExporter { *; }
-keep class software.amazon.opentelemetry.android.auth.kotlin.export.AwsSigV4SpanExporterBuilder { *; }

# Keep Kotlin metadata
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
