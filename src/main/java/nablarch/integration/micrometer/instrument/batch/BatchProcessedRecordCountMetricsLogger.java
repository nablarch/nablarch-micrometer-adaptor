package nablarch.integration.micrometer.instrument.batch;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import nablarch.core.ThreadContext;
import nablarch.core.log.app.CommitLogger;

/**
 * バッチの処理件数をメトリクスとして収集する{@link CommitLogger}の実装クラス。
 * <p>
 * メトリクスは、{@code "batch.processed.record.count"}という名前で作成される。<br>
 * また、以下のタグが設定される。
 * <ul>
 *   <li>{@code class} : バッチのアクションクラス名</li>
 * </ul>
 * </p>
 * @author Tanaka Tomoyuki
 */
public class BatchProcessedRecordCountMetricsLogger implements CommitLogger {
    /**
     * メトリクス名のデフォルト値。
     */
    static final String DEFAULT_METRICS_NAME = "batch.processed.record.count";
    /**
     * メトリクスの説明のデフォルト値。
     */
    static final String DEFAULT_METRICS_DESCRIPTION = "Count of processed records.";

    private MeterRegistry meterRegistry;
    private String metricsName = DEFAULT_METRICS_NAME;
    private String metricsDescription = DEFAULT_METRICS_DESCRIPTION;

    @Override
    public void increment(long count) {
        if (meterRegistry == null) {
            throw new IllegalStateException("meterRegistry is null.");
        }

        Tag tag = BatchActionClassTagUtil.obtain(ThreadContext.getRequestId());

        Counter.builder(metricsName)
                .tags(Tags.of(tag))
                .description(metricsDescription)
                .register(meterRegistry)
                .increment(count);
    }

    @Override
    public void initialize() {
        // noop
    }

    @Override
    public void terminate() {
        // noop
    }

    /**
     * メトリクス名を設定する。
     * @param metricsName メトリクス名
     */
    public void setMetricsName(String metricsName) {
        this.metricsName = metricsName;
    }

    /**
     * メトリクスの説明を設定する。
     * @param metricsDescription メトリクスの説明
     */
    public void setMetricsDescription(String metricsDescription) {
        this.metricsDescription = metricsDescription;
    }

    /**
     * {@link MeterRegistry}を設定する。
     * @param meterRegistry {@link MeterRegistry}
     */
    public void setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
}
