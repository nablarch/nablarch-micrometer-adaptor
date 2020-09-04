package nablarch.integration.micrometer.logging;

import io.micrometer.core.instrument.logging.LoggingRegistryConfig;
import nablarch.core.repository.di.DiContainer;
import nablarch.integration.micrometer.NablarchMeterRegistryConfig;

/**
 * {@link NablarchMeterRegistryConfig}を用いて{@link LoggingRegistryConfig}を実装したクラス。
 * @author Tanaka Tomoyuki
 */
public class NablarchLoggingRegistryConfig extends NablarchMeterRegistryConfig implements LoggingRegistryConfig {
    /**
     * コンストラクタ。
     * @param prefix プレフィックス
     * @param diContainer {@link DiContainer}
     */
    public NablarchLoggingRegistryConfig(String prefix, DiContainer diContainer) {
        super(prefix, diContainer);
    }

    @Override
    protected String subPrefix() {
        return "logging";
    }
}
