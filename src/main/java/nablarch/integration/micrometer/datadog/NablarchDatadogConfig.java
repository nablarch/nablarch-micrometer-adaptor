package nablarch.integration.micrometer.datadog;

import io.micrometer.datadog.DatadogConfig;
import nablarch.core.repository.di.DiContainer;
import nablarch.integration.micrometer.NablarchMeterRegistryConfig;

/**
 * {@link NablarchMeterRegistryConfig}を用いて{@link DatadogConfig}を実装したクラス。
 * @author Tanaka Tomoyuki
 */
public class NablarchDatadogConfig extends NablarchMeterRegistryConfig implements DatadogConfig {
    /**
     * コンストラクタ。
     * @param prefix プレフィックス
     * @param diContainer {@link DiContainer}
     */
    public NablarchDatadogConfig(String prefix, DiContainer diContainer) {
        super(prefix, diContainer);
    }

    @Override
    protected String subPrefix() {
        return "datadog";
    }
}
