package nablarch.integration.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import nablarch.core.repository.di.ComponentFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@link MeterRegistry}のコンポーネント生成に共通する処理をまとめた抽象クラス。
 *
 * @param <T> サブクラスで生成する具体的な{@link MeterRegistry}の型
 * @author Tanaka Tomoyuki
 */
public abstract class MeterRegistryFactory<T extends MeterRegistry> implements ComponentFactory<T> {
    /**
     * 設定値のプレフィックス。
     */
    protected String prefix;

    /**
     * 設定ファイルのパス。
     * <p>
     * 未設定の場合に読み込まれるデフォルトのパスについては{@link MicrometerConfiguration}を参照。
     * </p>
     */
    protected String xmlConfigPath;

    /**
     * 生成した{@link MeterRegistry}に適用する{@link io.micrometer.core.instrument.binder.MeterBinder MeterBinder}リストのプロバイダ。
     */
    protected MeterBinderListProvider meterBinderListProvider;

    /**
     * すべてのメトリクスに共通で設定するタグ。
     */
    protected Map<String, String> tags = Collections.emptyMap();

    protected T doCreateObject() {
        if (meterBinderListProvider == null) {
            throw new IllegalStateException("MeterBinderListProvider is not set.");
        }

        MicrometerConfiguration configuration = createMicrometerConfiguration();
        T meterRegistry = createMeterRegistry(configuration);

        setupCommonTags(meterRegistry);
        meterBinderListProvider.provide().forEach(meterBinder -> meterBinder.bindTo(meterRegistry));

        return meterRegistry;
    }

    private MicrometerConfiguration createMicrometerConfiguration() {
        if (xmlConfigPath == null) {
            return new MicrometerConfiguration();
        } else {
            return new MicrometerConfiguration(xmlConfigPath);
        }
    }

    private void setupCommonTags(MeterRegistry meterRegistry) {
        List<Tag> commonTagList = tags.entrySet()
                .stream()
                .map(entry -> Tag.of(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        meterRegistry.config().commonTags(commonTagList);
    }

    /**
     * {@link MeterRegistry}のインスタンスを生成する。
     * @param micrometerConfiguration Micrometerの設定
     * @return 生成した {@link MeterRegistry}のインスタンス
     */
    protected abstract T createMeterRegistry(MicrometerConfiguration micrometerConfiguration);

    /**
     * プレフィックスを設定する。
     * @param prefix プレフィックス
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * XML設定ファイルのパスを設定する。
     * @param xmlConfigPath XML設定ファイルのパス
     */
    public void setXmlConfigPath(String xmlConfigPath) {
        this.xmlConfigPath = xmlConfigPath;
    }

    /**
     * {@link MeterBinderListProvider}を設定する。
     * @param meterBinderListProvider {@link MeterBinderListProvider}
     */
    public void setMeterBinderListProvider(MeterBinderListProvider meterBinderListProvider) {
        this.meterBinderListProvider = meterBinderListProvider;
    }

    /**
     * すべてのメトリクスに共通で設定するタグを指定する。
     * @param tags すべてのメトリクスに共通で設定するタグ
     */
    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
}
