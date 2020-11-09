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
    private MeterRegistry meterRegistry;

    @Override
    public void initialize() {
        beginTimer();
    }

    @Override
    public void increment(long count) {
        Timer.Sample sample = (Timer.Sample)ThreadContext.getObject(THREAD_CONTEXT_KEY_TIMER_SAMPLE);
        Tag batchClass = BatchActionClassTagUtil.obtain(ThreadContext.getRequestId());
        sample.stop(meterRegistry, Timer.builder("batch.transaction.time")
                .description("Batch transaction time.")
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
}
