package nablarch.integration.micrometer.otlp;

import io.micrometer.registry.otlp.OtlpConfig;
import nablarch.core.repository.di.DiContainer;
import nablarch.integration.micrometer.NablarchMeterRegistryConfig;

/**
 * {@link NablarchMeterRegistryConfig}を用いて{@link OtlpConfig}を実装したクラス。
 * @author Junya Koyama
 */
public class NablarchOtlpConfig extends NablarchMeterRegistryConfig implements OtlpConfig {
    /**
     * プレフィックスと{@link DiContainer}を指定してインスタンスを生成する。
     *
     * @param prefix      プレフィックス
     * @param diContainer {@link DiContainer}
     */
    public NablarchOtlpConfig(String prefix, DiContainer diContainer) {
        super(prefix, diContainer);
    }

    @Override
    protected String subPrefix() {
        return "otlp";
    }
}
