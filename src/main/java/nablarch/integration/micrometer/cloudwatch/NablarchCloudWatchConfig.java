package nablarch.integration.micrometer.cloudwatch;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import nablarch.core.repository.di.DiContainer;
import nablarch.integration.micrometer.NablarchMeterRegistryConfig;

/**
 * {@link NablarchMeterRegistryConfig}を用いて{@link CloudWatchConfig}を実装したクラス。
 * @author Tanaka Tomoyuki
 */
public class NablarchCloudWatchConfig extends NablarchMeterRegistryConfig implements CloudWatchConfig {
    /**
     * コンストラクタ。
     * @param prefix プレフィックス
     * @param diContainer {@link DiContainer}
     */
    public NablarchCloudWatchConfig(String prefix, DiContainer diContainer) {
        super(prefix, diContainer);
    }

    @Override
    protected String subPrefix() {
        return "cloudwatch";
    }
}