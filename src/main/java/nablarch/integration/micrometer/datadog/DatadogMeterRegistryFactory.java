package nablarch.integration.micrometer.datadog;

import io.micrometer.datadog.DatadogConfig;
import io.micrometer.datadog.DatadogMeterRegistry;
import nablarch.integration.micrometer.MeterRegistryFactory;
import nablarch.integration.micrometer.MicrometerConfiguration;

/**
 * {@link DatadogMeterRegistry}のファクトリ。
 * @author Tanaka Tomoyuki
 */
public class DatadogMeterRegistryFactory extends MeterRegistryFactory<DatadogMeterRegistry> {

    @Override
    protected DatadogMeterRegistry createMeterRegistry(MicrometerConfiguration micrometerConfiguration) {
        DatadogConfig datadogConfig = new NablarchDatadogConfig(prefix, micrometerConfiguration);
        return DatadogMeterRegistry.builder(datadogConfig).build();
    }
}
