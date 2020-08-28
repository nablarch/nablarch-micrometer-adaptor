package nablarch.integration.micrometer.statsd;

import io.micrometer.statsd.StatsdConfig;
import nablarch.core.repository.di.DiContainer;
import nablarch.integration.micrometer.NablarchMeterRegistryConfig;

/**
 * {@link NablarchMeterRegistryConfig}を用いて{@link StatsdConfig}を実装したクラス。
 * @author Tanaka Tomoyuki
 */
public class NablarchStatsdConfig extends NablarchMeterRegistryConfig implements StatsdConfig {
    /**
     * プレフィックスと{@link DiContainer}を指定してインスタンスを生成する。
     *
     * @param prefix      プレフィックス
     * @param diContainer {@link DiContainer}
     */
    public NablarchStatsdConfig(String prefix, DiContainer diContainer) {
        super(prefix, diContainer);
    }

    @Override
    protected String subPrefix() {
        return "statsd";
    }
}
