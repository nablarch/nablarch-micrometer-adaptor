package nablarch.integration.micrometer.instrument.binder.logging;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import nablarch.core.log.basic.LogContext;
import nablarch.core.log.basic.LogLevel;
import nablarch.core.log.basic.LogListener;
import nablarch.core.log.basic.LogPublisher;
import org.junit.After;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * {@link LogCountMetrics}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class LogCountMetricsTest {
    private static final LogContext TRACE_LOG_CONTEXT = new LogContext("TEST_LOGGER", "TraceLogger", LogLevel.TRACE, "trace log", null);
    private static final LogContext DEBUG_LOG_CONTEXT = new LogContext("TEST_LOGGER", "DebugLogger", LogLevel.DEBUG, "debug log", null);
    private static final LogContext INFO_LOG_CONTEXT = new LogContext("TEST_LOGGER", "InfoLogger", LogLevel.INFO, "info log", null);
    private static final LogContext WARN_LOG_CONTEXT = new LogContext("TEST_LOGGER", "WarnLogger", LogLevel.WARN, "warn log", null);
    private static final LogContext ERROR_LOG_CONTEXT = new LogContext("TEST_LOGGER", "ErrorLogger", LogLevel.ERROR, "error log", null);
    private static final LogContext FATAL_LOG_CONTEXT = new LogContext("TEST_LOGGER", "FatalLogger", LogLevel.FATAL, "fatal log", null);

    private final LogCountMetrics sut = new LogCountMetrics();
    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final LogPublisher publisher = new LogPublisher();

    @Test
    public void testCountLogWriteUpperThanInfoInDefault() {
        sut.bindTo(registry);

        publisher.write(TRACE_LOG_CONTEXT);
        publisher.write(DEBUG_LOG_CONTEXT);
        publisher.write(INFO_LOG_CONTEXT);
        publisher.write(WARN_LOG_CONTEXT);
        publisher.write(ERROR_LOG_CONTEXT);
        publisher.write(FATAL_LOG_CONTEXT);

        assertThat(findCounter(TRACE_LOG_CONTEXT), is(nullValue()));
        assertThat(findCounter(DEBUG_LOG_CONTEXT), is(nullValue()));
        assertThat(findCounter(INFO_LOG_CONTEXT), is(nullValue()));

        assertThat(findCounter(WARN_LOG_CONTEXT), is(notNullValue()));
        assertThat(findCounter(ERROR_LOG_CONTEXT), is(notNullValue()));
        assertThat(findCounter(FATAL_LOG_CONTEXT), is(notNullValue()));
    }

    @Test
    public void testCustomLogLevel() {
        LogCountMetrics sut = new LogCountMetrics(LogLevel.INFO);
        sut.bindTo(registry);

        publisher.write(TRACE_LOG_CONTEXT);
        publisher.write(DEBUG_LOG_CONTEXT);
        publisher.write(INFO_LOG_CONTEXT);
        publisher.write(WARN_LOG_CONTEXT);
        publisher.write(ERROR_LOG_CONTEXT);
        publisher.write(FATAL_LOG_CONTEXT);

        assertThat(findCounter(TRACE_LOG_CONTEXT), is(nullValue()));
        assertThat(findCounter(DEBUG_LOG_CONTEXT), is(nullValue()));

        assertThat(findCounter(INFO_LOG_CONTEXT), is(notNullValue()));
        assertThat(findCounter(WARN_LOG_CONTEXT), is(notNullValue()));
        assertThat(findCounter(ERROR_LOG_CONTEXT), is(notNullValue()));
        assertThat(findCounter(FATAL_LOG_CONTEXT), is(notNullValue()));
    }

    @Test
    public void testCustomMetricsNameAndDescription() {
        String metricsName = "test.metrics";
        LogCountMetrics sut = new LogCountMetrics(metricsName, "Test metrics.");
        sut.bindTo(registry);

        publisher.write(TRACE_LOG_CONTEXT);
        publisher.write(DEBUG_LOG_CONTEXT);
        publisher.write(INFO_LOG_CONTEXT);
        publisher.write(WARN_LOG_CONTEXT);
        publisher.write(ERROR_LOG_CONTEXT);
        publisher.write(FATAL_LOG_CONTEXT);

        assertThat(findCounter(TRACE_LOG_CONTEXT, metricsName), is(nullValue()));
        assertThat(findCounter(DEBUG_LOG_CONTEXT, metricsName), is(nullValue()));
        assertThat(findCounter(INFO_LOG_CONTEXT, metricsName), is(nullValue()));

        assertThat(findCounter(WARN_LOG_CONTEXT, metricsName), is(notNullValue()));
        assertThat(findCounter(ERROR_LOG_CONTEXT, metricsName), is(notNullValue()));
        assertThat(findCounter(FATAL_LOG_CONTEXT, metricsName), is(notNullValue()));

        assertThat(findCounter(WARN_LOG_CONTEXT, metricsName).getId().getDescription(), is("Test metrics."));
    }

    @Test
    public void testCustomMetricsNameAndDescriptionAndLogLevel() {
        String metricsName = "test.metrics";
        LogCountMetrics sut = new LogCountMetrics(metricsName, "Test metrics.", LogLevel.ERROR);
        sut.bindTo(registry);

        publisher.write(TRACE_LOG_CONTEXT);
        publisher.write(DEBUG_LOG_CONTEXT);
        publisher.write(INFO_LOG_CONTEXT);
        publisher.write(WARN_LOG_CONTEXT);
        publisher.write(ERROR_LOG_CONTEXT);
        publisher.write(FATAL_LOG_CONTEXT);

        assertThat(findCounter(TRACE_LOG_CONTEXT, metricsName), is(nullValue()));
        assertThat(findCounter(DEBUG_LOG_CONTEXT, metricsName), is(nullValue()));
        assertThat(findCounter(INFO_LOG_CONTEXT, metricsName), is(nullValue()));
        assertThat(findCounter(WARN_LOG_CONTEXT, metricsName), is(nullValue()));

        assertThat(findCounter(ERROR_LOG_CONTEXT, metricsName), is(notNullValue()));
        assertThat(findCounter(FATAL_LOG_CONTEXT, metricsName), is(notNullValue()));

        assertThat(findCounter(FATAL_LOG_CONTEXT, metricsName).getId().getDescription(), is("Test metrics."));
    }

    @Test
    public void testCountNumberOfLoggingEvent() {
        sut.bindTo(registry);

        publisher.write(WARN_LOG_CONTEXT);

        publisher.write(ERROR_LOG_CONTEXT);
        publisher.write(ERROR_LOG_CONTEXT);

        publisher.write(FATAL_LOG_CONTEXT);
        publisher.write(FATAL_LOG_CONTEXT);
        publisher.write(FATAL_LOG_CONTEXT);

        assertThat(findCounter(WARN_LOG_CONTEXT).count(), is(1.0));
        assertThat(findCounter(ERROR_LOG_CONTEXT).count(), is(2.0));
        assertThat(findCounter(FATAL_LOG_CONTEXT).count(), is(3.0));

        assertThat(findCounter(WARN_LOG_CONTEXT).getId().getDescription(), is(LogCountMetrics.DEFAULT_METRICS_DESCRIPTION));
        assertThat(findCounter(ERROR_LOG_CONTEXT).getId().getDescription(), is(LogCountMetrics.DEFAULT_METRICS_DESCRIPTION));
        assertThat(findCounter(FATAL_LOG_CONTEXT).getId().getDescription(), is(LogCountMetrics.DEFAULT_METRICS_DESCRIPTION));
    }

    @Test
    public void testClose() {
        sut.bindTo(registry);

        MockLogListener mockLogListener = new MockLogListener();
        LogPublisher.addListener(mockLogListener);

        publisher.write(WARN_LOG_CONTEXT);

        sut.close();

        publisher.write(WARN_LOG_CONTEXT);

        assertThat(findCounter(WARN_LOG_CONTEXT).count(), is(1.0));
        assertThat(mockLogListener.count, is(2));
    }

    private static class MockLogListener implements LogListener {
        private int count;

        @Override
        public void onWritten(LogContext context) {
            count++;
        }
    }

    @After
    public void tearDown() {
        LogPublisher.removeAllListeners();
    }

    private Counter findCounter(LogContext logContext) {
        return findCounter(logContext, LogCountMetrics.DEFAULT_METRICS_NAME);
    }

    private Counter findCounter(LogContext logContext, String metricsName) {
        return registry.find(metricsName)
                .tag(LogCountMetrics.TAG_NAME_RUNTIME_LOGGER, logContext.getRuntimeLoggerName())
                .tag(LogCountMetrics.TAG_NAME_LEVEL, logContext.getLevel().name())
                .counter();
    }
}