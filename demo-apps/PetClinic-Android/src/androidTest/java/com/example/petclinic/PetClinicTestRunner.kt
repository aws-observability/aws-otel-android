package com.example.petclinic

import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner

class PetClinicTestRunner : AndroidJUnitRunner() {
    override fun onCreate(arguments: Bundle) {
        arguments.getString("deviceFarmJobId")?.let { PetClinicApplication.deviceFarmJobId = it }
        arguments.getString("appMonitorId")?.let { PetClinicApplication.appMonitorId = it }
        arguments.getString("appMonitorRegion")?.let { PetClinicApplication.appMonitorRegion = it }
        arguments.getString("environmentSuffix")?.let { PetClinicApplication.environmentSuffix = it }
        super.onCreate(arguments)
    }
}
