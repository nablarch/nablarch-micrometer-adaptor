package nablarch.integration.micrometer.statsd;

import io.micrometer.statsd.StatsdMeterRegistry;
import nablarch.integration.micrometer.MeterRegistryFactory;
import nablarch.integration.micrometer.MicrometerConfiguration;

/**
 * {@link StatsdMeterRegistry}のファクトリ。
 * @author Tanaka Tomoyuki
 */
public class StatsdMeterRegistryFactory extends MeterRegistryFactory<StatsdMeterRegistry> {

    @Override
    protected StatsdMeterRegistry createMeterRegistry(MicrometerConfiguration micrometerConfiguration) {
        NablarchStatsdConfig config = new NablarchStatsdConfig(prefix, micrometerConfiguration);
        return StatsdMeterRegistry.builder(config).build();
    }
}
