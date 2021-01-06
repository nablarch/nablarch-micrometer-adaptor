package nablarch.integration.micrometer.instrument.binder.jvm;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import nablarch.integration.micrometer.instrument.binder.MetricsMetaData;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.List;

/**
 * GCの発生回数をカウントする{@link MeterBinder}。
 * <p>
 * {@code jvm.gc.count} という名前のメトリクスが、メモリマネージャの数だけ登録される。<br>
 * 各メトリクスには、メモリマネージャの名前が {@code memory.manager.name} タグで設定される。
 * </p>
 * @author Tanaka Tomoyuki
 */
public class NablarchGcCountMetrics implements MeterBinder {
    /** デフォルトのメトリクス名。 */
    static final String DEFAULT_METRICS_NAME = "jvm.gc.count";
    /** デフォルトのメトリクスの説明。 */
    static final String DEFAULT_METRICS_DESCRIPTION = "Count of garbage collection";

    /** 追加のタグ一覧。 */
    private final Iterable<Tag> tags;
    /** メトリクス名。 */
    private final String metricsName;
    /** メトリクスの説明。 */
    private final String metricsDescription;

    /**
     * コンストラクタ。
     */
    public NablarchGcCountMetrics() {
        this(Collections.emptyList());
    }

    /**
     * メトリクス名と説明を設定するコンストラクタ。
     * @param metricsName メトリクス名
     * @param metricsDescription メトリクスの説明
     */
    public NablarchGcCountMetrics(String metricsName, String metricsDescription) {
        this(metricsName, metricsDescription, Collections.emptyList());
    }

    /**
     * 追加のタグを指定するコンストラクタ。
     * @param tags 追加で指定するタグ
     */
    public NablarchGcCountMetrics(Iterable<Tag> tags) {
        this(DEFAULT_METRICS_NAME, DEFAULT_METRICS_DESCRIPTION, tags);
    }

    /**
     * メトリクス名と説明、追加のタグを{@link MetricsMetaData}で指定するコンストラクタ。
     * @param metricsMetaData メトリクスの設定情報
     */
    public NablarchGcCountMetrics(MetricsMetaData metricsMetaData) {
        this.metricsName = metricsMetaData.getName();
        this.metricsDescription = metricsMetaData.getDescription();
        this.tags = metricsMetaData.getTags();
    }

    /**
     * メトリクス名と説明、追加のタグを指定するコンストラクタ。
     * @param metricsName メトリクス名
     * @param metricsDescription メトリクスの説明
     * @param tags 追加で指定するタグ
     */
    public NablarchGcCountMetrics(String metricsName, String metricsDescription, Iterable<Tag> tags) {
        this.metricsName = metricsName;
        this.metricsDescription = metricsDescription;
        this.tags = tags;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();

        for (GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMXBeans) {
            FunctionCounter.builder(metricsName, garbageCollectorMXBean, GarbageCollectorMXBean::getCollectionCount)
                    .tag("memory.manager.name", garbageCollectorMXBean.getName())
                    .tags(tags)
                    .description(metricsDescription)
                    .register(registry);
        }
    }
}
