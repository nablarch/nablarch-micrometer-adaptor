package nablarch.integration.micrometer.logging;

import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import nablarch.integration.micrometer.MeterRegistryFactory;
import nablarch.integration.micrometer.MicrometerConfiguration;

/**
 * {@link LoggingMeterRegistry}のファクトリ。
 * @author Tanaka Tomoyuki
 */
public class LoggingMeterRegistryFactory extends MeterRegistryFactory<LoggingMeterRegistry> {

    @Override
    public LoggingMeterRegistry createObject() {
        return doCreateObject();
    }

    @Override
    protected LoggingMeterRegistry createMeterRegistry(MicrometerConfiguration micrometerConfiguration) {
        NablarchLoggingRegistryConfig config = new NablarchLoggingRegistryConfig(prefix, micrometerConfiguration);
        return LoggingMeterRegistry.builder(config).build();
    }
}
