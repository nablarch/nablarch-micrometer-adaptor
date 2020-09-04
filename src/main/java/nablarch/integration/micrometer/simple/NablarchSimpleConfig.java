package nablarch.integration.micrometer.simple;

import io.micrometer.core.instrument.simple.SimpleConfig;
import nablarch.core.repository.di.DiContainer;
import nablarch.integration.micrometer.NablarchMeterRegistryConfig;

/**
 * {@link NablarchMeterRegistryConfig}を用いて{@link SimpleConfig}を実装したクラス。
 * @author Tanaka Tomoyuki
 */
public class NablarchSimpleConfig extends NablarchMeterRegistryConfig implements SimpleConfig {

    /**
     * プレフィックスと{@link DiContainer}を指定してインスタンスを生成する。
     *
     * @param prefix      プレフィックス
     * @param diContainer {@link DiContainer}
     */
    public NablarchSimpleConfig(String prefix, DiContainer diContainer) {
        super(prefix, diContainer);
    }

    @Override
    protected String subPrefix() {
        return "simple";
    }
}
