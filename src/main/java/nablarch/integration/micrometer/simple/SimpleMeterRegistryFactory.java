package nablarch.integration.micrometer.simple;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import nablarch.integration.micrometer.MeterRegistryFactory;
import nablarch.integration.micrometer.MicrometerConfiguration;

/**
 * {@link SimpleMeterRegistry}のファクトリ。
 * @author Tanaka Tomoyuki
 */
public class SimpleMeterRegistryFactory extends MeterRegistryFactory<SimpleMeterRegistry> {

    @Override
    public SimpleMeterRegistry createObject() {
        return doCreateObject();
    }

    @Override
    protected SimpleMeterRegistry createMeterRegistry(MicrometerConfiguration micrometerConfiguration) {
        NablarchSimpleConfig config = new NablarchSimpleConfig(prefix, micrometerConfiguration);
        return new SimpleMeterRegistry(config, Clock.SYSTEM);
    }
}
