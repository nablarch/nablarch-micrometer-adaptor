package nablarch.integration.micrometer.instrument.batch;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import nablarch.core.ThreadContext;
import nablarch.core.log.app.CommitLogger;

import java.util.Collections;

/**
 * バッチのトランザクションごとの処理時間をメトリクスとして計測するロガー。
 * <p>
 * メトリクスは、{@code "batch.transaction.time"}という名前で作成される。
 * </p>
 *
 * @author Tanaka Tomoyuki
 */
public class BatchTransactionTimeMetricsLogger implements CommitLogger {
    private static final String THREAD_CONTEXT_KEY_TIMER_SAMPLE = "nablarch_transaction_timer_sample";

    /**
     * デフォルトのメトリクス名。
     */
    static final String DEFAULT_METRICS_NAME = "batch.transaction.time";

    /**
     * デフォルトのメトリクスの説明。
     */
    static final String DEFAULT_METRICS_DESCRIPTION = "Batch transaction time.";

    private MeterRegistry meterRegistry;
    private String metricsName = DEFAULT_METRICS_NAME;
    private String metricsDescription = DEFAULT_METRICS_DESCRIPTION;

    @Override
    public void initialize() {
        beginTimer();
    }

    @Override
    public void increment(long count) {
        Timer.Sample sample = (Timer.Sample)ThreadContext.getObject(THREAD_CONTEXT_KEY_TIMER_SAMPLE);
        Tag batchClass = BatchActionClassTagUtil.obtain(ThreadContext.getRequestId());
        sample.stop(meterRegistry, Timer.builder(metricsName)
                .description(metricsDescription)
                .tags(Collections.singleton(batchClass)));

        beginTimer();
    }

    private void beginTimer() {
        Timer.Sample sample = Timer.start(meterRegistry);
        ThreadContext.setObject(THREAD_CONTEXT_KEY_TIMER_SAMPLE, sample);
    }

    @Override
    public void terminate() {
        // noop
    }

    /**
     * {@link MeterRegistry}を設定する。
     * @param meterRegistry {@link MeterRegistry}
     */
    public void setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * メトリクスの名前を設定する。
     * @param metricsName メトリクスの名前
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
}
