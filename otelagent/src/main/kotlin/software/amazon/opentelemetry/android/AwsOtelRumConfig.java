/*
 * Copyright Amazon.com, Inc. or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package software.amazon.opentelemetry.android;

import io.opentelemetry.android.config.OtelRumConfig;
import io.opentelemetry.android.features.diskbuffering.DiskBufferingConfig;
import io.opentelemetry.api.common.Attributes;
import java.util.function.Supplier;

public class AwsOtelRumConfig extends OtelRumConfig {

    private String awsRegion = "";
    private String rumAppMonitor = "";

    public AwsOtelRumConfig setGlobalAttributes(Attributes attributes) {
        return setGlobalAttributes(() -> attributes);
    }

    public AwsOtelRumConfig setGlobalAttributes(Supplier<Attributes> globalAttributesSupplier) {
        super.setGlobalAttributes(globalAttributesSupplier);
        return this;
    }

    public AwsOtelRumConfig disableNetworkAttributes() {
        super.disableNetworkAttributes();
        return this;
    }

    public AwsOtelRumConfig disableSdkInitializationEvents() {
        super.disableSdkInitializationEvents();
        return this;
    }

    public AwsOtelRumConfig disableScreenAttributes() {
        super.disableScreenAttributes();
        return this;
    }

    public AwsOtelRumConfig disableInstrumentationDiscovery() {
        super.disableInstrumentationDiscovery();
        return this;
    }

    public AwsOtelRumConfig setDiskBufferingConfig(DiskBufferingConfig diskBufferingConfig) {
        super.setDiskBufferingConfig(diskBufferingConfig);
        return this;
    }

    public AwsOtelRumConfig(String awsRegion, String rumAppMonitor) {
        super();
        this.awsRegion = awsRegion;
        this.rumAppMonitor = rumAppMonitor;
    }

    public AwsOtelRumConfig() {
        super();
    }

    public AwsOtelRumConfig setAwsRegion(String awsRegion) {
        this.awsRegion = awsRegion;
        return this;
    }

    public AwsOtelRumConfig setRumAppMonitor(String rumAppMonitor) {
        this.rumAppMonitor = rumAppMonitor;
        return this;
    }

    public String getAwsRegion() {
        return this.awsRegion;
    }

    public String getRumAppMonitor() {
        return this.rumAppMonitor;
    }
}
