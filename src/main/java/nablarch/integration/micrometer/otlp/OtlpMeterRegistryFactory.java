package nablarch.integration.micrometer.otlp;

import io.micrometer.core.instrument.Clock;
import io.micrometer.registry.otlp.OtlpMeterRegistry;
import nablarch.integration.micrometer.MeterRegistryFactory;
import nablarch.integration.micrometer.MicrometerConfiguration;

/**
 * {@link OtlpMeterRegistry}のファクトリ。
 * @author Junya Koyama
 */
public class OtlpMeterRegistryFactory extends MeterRegistryFactory<OtlpMeterRegistry> {

    @Override
    public OtlpMeterRegistry createObject() {
        return doCreateObject();
    }

    @Override
    protected OtlpMeterRegistry createMeterRegistry(MicrometerConfiguration micrometerConfiguration) {
        NablarchOtlpConfig config = new NablarchOtlpConfig(prefix, micrometerConfiguration);
        return new OtlpMeterRegistry(config, Clock.SYSTEM);
    }
}
