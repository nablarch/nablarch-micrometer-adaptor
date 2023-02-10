package nablarch.integration.micrometer.instrument.binder.logging;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import nablarch.core.log.basic.LogContext;
import nablarch.core.log.basic.LogLevel;
import nablarch.core.log.basic.LogListener;
import nablarch.core.log.basic.LogPublisher;
import nablarch.integration.micrometer.instrument.binder.MetricsMetaData;
import org.junit.After;
import org.junit.Test;

import java.util.Arrays;

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
    public void testCustomMetricsMetaData() {
        String metricsName = "test.metrics";
        MetricsMetaData metricsMetaData = new MetricsMetaData(
                metricsName, "Test metrics.", Arrays.asList(Tag.of("foo", "FOO"), Tag.of("bar", "BAR")));
        LogCountMetrics sut = new LogCountMetrics(metricsMetaData);
        sut.bindTo(registry);

        publisher.write(TRACE_LOG_CONTEXT);
        publisher.write(DEBUG_LOG_CONTEXT);
        publisher.write(INFO_LOG_CONTEXT);
        publisher.write(WARN_LOG_CONTEXT);
        publisher.write(ERROR_LOG_CONTEXT);
        publisher.write(FATAL_LOG_CONTEXT);

        /*
         * ログレベルのしきい値を検証をしている理由について
         *
         * LogCountMetrics(MetricsMetaData)は、ログレベルのしきい値を受け取らない。
         * にもかかわらず、本テスト内ではログレベルのしきい値を検証している。
         *
         * これは、デフォルトのログレベルのしきい値(DEFAULT_LOG_LEVEL)が
         * 正しく使用されていることを確認することが目的となっている。
         */
        assertThat(findCounter(TRACE_LOG_CONTEXT, metricsName), is(nullValue()));
        assertThat(findCounter(DEBUG_LOG_CONTEXT, metricsName), is(nullValue()));
        assertThat(findCounter(INFO_LOG_CONTEXT, metricsName), is(nullValue()));

        assertThat(findCounter(WARN_LOG_CONTEXT, metricsName), is(notNullValue()));
        assertThat(findCounter(ERROR_LOG_CONTEXT, metricsName), is(notNullValue()));
        assertThat(findCounter(FATAL_LOG_CONTEXT, metricsName), is(notNullValue()));

        Meter.Id warnLogCounterId = findCounter(WARN_LOG_CONTEXT, metricsName).getId();
        assertThat(warnLogCounterId.getDescription(), is("Test metrics."));
        assertThat(warnLogCounterId.getTags(), hasItem(Tag.of("foo", "FOO")));
        assertThat(warnLogCounterId.getTags(), hasItem(Tag.of("bar", "BAR")));
    }

    @Test
    public void testCustomMetricsMetaDataAndLogLevel() {
        String metricsName = "test.metrics";
        MetricsMetaData metricsMetaData = new MetricsMetaData(
                metricsName, "Test metrics.", Arrays.asList(Tag.of("foo", "FOO"), Tag.of("bar", "BAR")));
        LogCountMetrics sut = new LogCountMetrics(metricsMetaData, LogLevel.ERROR);
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

        Meter.Id fatalLogCounterId = findCounter(FATAL_LOG_CONTEXT, metricsName).getId();
        assertThat(fatalLogCounterId.getDescription(), is("Test metrics."));
        assertThat(fatalLogCounterId.getTags(), hasItem(Tag.of("foo", "FOO")));
        assertThat(fatalLogCounterId.getTags(), hasItem(Tag.of("bar", "BAR")));
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

    @Test
    public void testCountForEachLogLevels() {
        sut.bindTo(registry);

        String runtimeLoggerName = "TestLogger";
        LogContext warnContext = new LogContext("TEST", runtimeLoggerName, LogLevel.WARN, "warn context", null);
        LogContext errorContext = new LogContext("TEST", runtimeLoggerName, LogLevel.ERROR, "error context", null);

        publisher.write(warnContext);
        publisher.write(errorContext);

        assertThat(findCounter(warnContext).count(), is(1.0));
        assertThat(findCounter(errorContext).count(), is(1.0));
    }

    @Test
    public void testCountForEachRuntimeLoggers() {
        sut.bindTo(registry);

        LogLevel logLevel = LogLevel.WARN;
        LogContext fooWarnContext = new LogContext("TEST", "foo", logLevel, "foo warn context", null);
        LogContext barWarnContext = new LogContext("TEST", "bar", logLevel, "bar warn context", null);

        publisher.write(fooWarnContext);
        publisher.write(barWarnContext);

        assertThat(findCounter(fooWarnContext).count(), is(1.0));
        assertThat(findCounter(barWarnContext).count(), is(1.0));
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

    /**
     * 指定されたログコンテキストの出力をカウントした {@link Counter} を、デフォルトのメトリクス名で検索する。
     * <p>
     * 該当する {@link Counter} が見つからない場合は {@code null} を返す。
     * </p>
     * @param logContext カウント対象のログコンテキスト
     * @return ログコンテキストの出力をカウントした {@link Counter}
     */
    private Counter findCounter(LogContext logContext) {
        return findCounter(logContext, LogCountMetrics.DEFAULT_METRICS_NAME);
    }

    /**
     * 指定されたログコンテキストの出力をカウントした {@link Counter} を、指定されたメトリクス名で検索する。
     * <p>
     * 該当する {@link Counter} が見つからない場合は {@code null} を返す。
     * </p>
     * @param logContext カウント対象のログコンテキスト
     * @param metricsName メトリクス名
     * @return ログコンテキストの出力をカウントした {@link Counter}
     */
    private Counter findCounter(LogContext logContext, String metricsName) {
        return registry.find(metricsName)
                .tag(LogCountMetrics.TAG_NAME_RUNTIME_LOGGER, logContext.getRuntimeLoggerName())
                .tag(LogCountMetrics.TAG_NAME_LEVEL, logContext.getLevel().name())
                .counter();
    }
}