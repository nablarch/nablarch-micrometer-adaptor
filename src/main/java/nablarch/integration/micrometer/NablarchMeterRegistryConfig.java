package nablarch.integration.micrometer;

import io.micrometer.core.instrument.config.MeterRegistryConfig;
import nablarch.core.repository.di.DiContainer;

/**
 * {@link DiContainer}がロードした情報で設定値を解決する{@link MeterRegistryConfig}の実装クラス。
 * @author Tanaka Tomoyuki
 */
public abstract class NablarchMeterRegistryConfig implements MeterRegistryConfig {
    /** 設定名のプレフィックス。 */
    private final String prefix;
    /** 設定値の取得で使用する {@link DiContainer}。 */
    private final DiContainer diContainer;

    /**
     * プレフィックスと{@link DiContainer}を指定してインスタンスを生成する。
     * @param prefix プレフィックス
     * @param diContainer {@link DiContainer}
     */
    protected NablarchMeterRegistryConfig(String prefix, DiContainer diContainer) {
        this.prefix = prefix;
        this.diContainer = diContainer;
    }

    /**
     * プレフィックスを取得する。
     * <p>
     * コンストラクタで指定された{@code prefix}が{@code null}の場合は、{@code "nablarch.micrometer." + subPrefix()}を返す。<br>
     * {@code prefix}が{@code null}でない場合は、その値をそのまま返す。
     * </p>
     *
     * @return プレフィックス
     */
    @Override
    public String prefix() {
        return prefix == null ? "nablarch.micrometer." + subPrefix() : prefix;
    }

    /**
     * サブプレフィックスを取得する。
     * @return サブプレフィックス
     */
    protected abstract String subPrefix();

    @Override
    public String get(String key) {
        return diContainer.getComponentByName(key);
    }
}
