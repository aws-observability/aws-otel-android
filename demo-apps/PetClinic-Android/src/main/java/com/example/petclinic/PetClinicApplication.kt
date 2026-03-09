package com.example.petclinic

import android.app.Application
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.sdk.resources.Resource
import software.amazon.opentelemetry.android.OpenTelemetryRumClient

class PetClinicApplication : Application() {
    companion object {
        var deviceFarmJobId: String = "test-device-farm-job-id"
        var appMonitorId: String = "test-app-monitor-id-uuid"
        var appMonitorRegion: String = "us-west-2"
        var environmentSuffix: String = ""
    }

    override fun onCreate() {
        super.onCreate()
        val exportEndpoint = "https://dataplane.rum${environmentSuffix}.${appMonitorRegion}.amazonaws.com/v1/rum"

        OpenTelemetryRumClient {
            androidApplication = this@PetClinicApplication
            awsRum {
                region = appMonitorRegion
                appMonitorId = Companion.appMonitorId
            }
            spanExporter = OtlpHttpSpanExporter.builder()
                .setEndpoint(exportEndpoint)
                .build()
            logRecordExporter = OtlpHttpLogRecordExporter.builder()
                .setEndpoint(exportEndpoint)
                .build()
            otelResource = Resource.builder()
                .put("service.name", "PetClinic-Android")
                .put("service.version", "1.0.0")
                .put("deviceFarmJobId", deviceFarmJobId)
                .build()
        }
    }
}
