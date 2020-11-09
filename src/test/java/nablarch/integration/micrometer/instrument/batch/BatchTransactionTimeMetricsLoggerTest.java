package nablarch.integration.micrometer.instrument.batch;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import nablarch.core.ThreadContext;
import nablarch.integration.micrometer.instrument.batch.BatchTransactionTimeMetricsLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * {@link BatchTransactionTimeMetricsLogger}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class BatchTransactionTimeMetricsLoggerTest {
    private BatchTransactionTimeMetricsLogger sut = new BatchTransactionTimeMetricsLogger();
    private String originalRequestId;
    @Before
    public void setup() {
        originalRequestId = ThreadContext.getRequestId();
        ThreadContext.setRequestId("TestBatchAction/test");
    }

    @Test
    public void testMeasureTime() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry(SimpleConfig.DEFAULT, new Clock() {
            // 奇数番目の値が計測開始時刻、偶数番目が計測終了時刻を表している。
            Iterator<Long> monotonicTimes = Arrays.asList(
                1000L, 2500L,
                3000L, 3500L,
                4000L).iterator();

            @Override
            public long monotonicTime() {
                return monotonicTimes.next();
            }

            @Override
            public long wallTime() {
                return 0;
            }
        });
        sut.setMeterRegistry(meterRegistry);

        sut.initialize();
        sut.increment(0L);
        Timer timer = meterRegistry.find("batch.transaction.time").timer();
        assertThat(timer.getId().getTag("class"), is("TestBatchAction"));

        assertThat(timer.count(), is(1L));
        assertThat(timer.totalTime(TimeUnit.NANOSECONDS), is(1500.0));

        sut.increment(0L);
        assertThat(timer.count(), is(2L));
        assertThat(timer.totalTime(TimeUnit.NANOSECONDS), is(2000.0));
    }

    @After
    public void teardown() {
        sut.terminate();
        ThreadContext.setRequestId(originalRequestId);
    }
}