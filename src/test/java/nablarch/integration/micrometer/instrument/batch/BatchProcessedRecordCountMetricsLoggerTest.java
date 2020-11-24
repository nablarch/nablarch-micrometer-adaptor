package nablarch.integration.micrometer.instrument.batch;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import nablarch.core.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;

/**
 * {@link BatchProcessedRecordCountMetricsLogger}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class BatchProcessedRecordCountMetricsLoggerTest {
    private final BatchProcessedRecordCountMetricsLogger sut = new BatchProcessedRecordCountMetricsLogger();
    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private String originalRequestId;

    @Before
    public void setUp() {
        originalRequestId = ThreadContext.getRequestId();
        ThreadContext.setRequestId("TestAction/test");
        sut.setMeterRegistry(meterRegistry);
    }

    @Test
    public void testIncrement() {
        sut.increment(3L);

        Counter counter = meterRegistry.find(BatchProcessedRecordCountMetricsLogger.DEFAULT_METRICS_NAME).counter();
        assertThat(counter.count(), is(3.0));
        assertThat(counter.getId().getDescription(), is(BatchProcessedRecordCountMetricsLogger.DEFAULT_METRICS_DESCRIPTION));
        assertThat(counter.getId().getTag("class"), is("TestAction"));
    }

    @Test
    public void testSetMetricsName() {
        sut.setMetricsName("test.metrics");
        sut.increment(2L);

        Counter counter = meterRegistry.find("test.metrics").counter();
        assertThat(counter.count(), is(2.0));
    }

    @Test
    public void testSetMetricsDescription() {
        sut.setMetricsDescription("Test metrics.");
        sut.increment(2L);

        Counter counter = meterRegistry.find(BatchProcessedRecordCountMetricsLogger.DEFAULT_METRICS_NAME).counter();
        assertThat(counter.getId().getDescription(), is("Test metrics."));
    }

    @Test
    public void testThrowExceptionIfMeterRegistryIsNull() {
        sut.setMeterRegistry(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sut.increment(1L));

        assertThat(exception.getMessage(), is("meterRegistry is null."));
    }

    @Test
    public void testNoopMethods() {
        /*
         * カバレッジを通すため、何もしないメソッドを実行するテストを走らせている。
         * ひとまず、例外がスローされないことだけを確認。
         */
        sut.initialize();
        sut.terminate();
    }

    @After
    public void tearDown() {
        ThreadContext.setRequestId(originalRequestId);
    }
}