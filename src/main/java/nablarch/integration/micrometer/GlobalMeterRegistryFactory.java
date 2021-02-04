package nablarch.integration.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;

/**
 * Micrometerのグローバルレジストリ({@code io.micrometer.core.instrument.Metrics.globalRegistry})を
 * コンポーネントとして生成するファクトリクラス。
 * @author Tanaka Tomoyuki
 */
public class GlobalMeterRegistryFactory extends MeterRegistryFactory<MeterRegistry> {

    @Override
    public MeterRegistry createObject() {
        return doCreateObject();
    }

    @Override
    protected MeterRegistry createMeterRegistry(MicrometerConfiguration micrometerConfiguration) {
        return Metrics.globalRegistry;
    }
}
