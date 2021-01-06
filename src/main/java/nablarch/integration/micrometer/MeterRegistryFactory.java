package nablarch.integration.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import nablarch.core.repository.di.ComponentFactory;
import nablarch.core.repository.disposal.ApplicationDisposer;

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

    /**
     * 廃棄処理を行うインタフェース。
     */
    protected ApplicationDisposer applicationDisposer;

    /**
     * {@link ComponentFactory#createObject()} の実処理を行うメソッド。
     * <p>
     * サブクラスは、本メソッドを使って {@code createObject()} を次のように実装する。
     * <pre><code>{@literal @}Override
     * public SimpleMeterRegistry createObject() {
     *     return doCreateObject();
     * }</code></pre>
     * </p>
     * <p>
     * これは、 {@code createObject()} の戻り値の型が総称型だった場合、
     * DIコンテナがコンポーネントの具象型を特定できないことに起因する。<br>
     * この問題は、上述のようにサブクラスで {@code createObject()} の戻り値の型を具象型として宣言することで回避できる。<br>
     * 一方で、コンポーネントを作成するロジック自体はどの {@link MeterRegistry} でも共通なので、
     * コンポーネント作成処理を共通化するために、このメソッドが用意されている。
     * </p>
     * @return 作成された {@link MeterRegistry} オブジェクト
     */
    protected T doCreateObject() {
        if (meterBinderListProvider == null) {
            throw new IllegalStateException("MeterBinderListProvider is not set.");
        }
        if (applicationDisposer == null) {
            throw new IllegalStateException("ApplicationDisposer is not set.");
        }

        MicrometerConfiguration configuration = createMicrometerConfiguration();
        T meterRegistry = createMeterRegistry(configuration);

        setupCommonTags(meterRegistry);
        meterBinderListProvider.provide().forEach(meterBinder -> meterBinder.bindTo(meterRegistry));

        applicationDisposer.addDisposable(meterRegistry::close);

        return meterRegistry;
    }

    /**
     * {@link MicrometerConfiguration} を生成する。
     * @return 生成された {@link MicrometerConfiguration}
     */
    private MicrometerConfiguration createMicrometerConfiguration() {
        if (xmlConfigPath == null) {
            return new MicrometerConfiguration();
        } else {
            return new MicrometerConfiguration(xmlConfigPath);
        }
    }

    /**
     * 全てのメトリクスに共通して設定するタグをセットアップする。
     * @param meterRegistry 設定対象の {@link MeterRegistry}
     */
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

    /**
     * {@link ApplicationDisposer}を設定する。
     * @param applicationDisposer {@link ApplicationDisposer}
     */
    public void setApplicationDisposer(ApplicationDisposer applicationDisposer) {
        this.applicationDisposer = applicationDisposer;
    }
}
