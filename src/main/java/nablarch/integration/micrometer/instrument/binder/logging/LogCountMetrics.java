package nablarch.integration.micrometer.instrument.binder.logging;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import nablarch.core.log.basic.LogLevel;
import nablarch.core.log.basic.LogListener;
import nablarch.core.log.basic.LogPublisher;

import java.io.Closeable;

/**
 * ログレベルごとのログ出力回数をメトリクスとして収集する{@link MeterBinder}。
 * <p>
 * メトリクス名は{@code log.count}になる。<br>
 * また、メトリクスのタグには以下の値が設定される。
 * <ul>
 *   <li>{@code level}: ログレベル</li>
 *   <li>{@code logger}: 実行時ロガー名({@code LoggerManager.get(String)} の引数で渡した名前)</li>
 * </ul>
 * </p>
 * <p>
 * デフォルトでは{@code WARN}以上のログのみを集計する。<br>
 * </p>
 * @author Tanaka Tomoyuki
 */
public class LogCountMetrics implements MeterBinder, Closeable {
    /**
     * デフォルトのメトリクスの名前。
     */
    static final String DEFAULT_METRICS_NAME = "log.count";

    /**
     * デフォルトのメトリクスの説明。
     */
    static final String DEFAULT_METRICS_DESCRIPTION = "Logging count for each log level and runtime logger.";

    /**
     * デフォルトのログレベル。
     */
    private static final LogLevel DEFAULT_LOG_LEVEL = LogLevel.WARN;

    /**
     * ログレベルのタグ名。
     */
    static final String TAG_NAME_LEVEL = "level";

    /**
     * 実行時ロガー名のタグ名。
     */
    static final String TAG_NAME_RUNTIME_LOGGER = "logger";

    private final LogLevel logLevel;
    private final String metricsName;
    private final String metricsDescription;
    private LogListener logListener;

    /**
     * デフォルトコンストラクタ。
     * <p>
     * ログレベルは{@link LogLevel#WARN}になる。
     * </p>
     */
    public LogCountMetrics() {
        this(DEFAULT_LOG_LEVEL);
    }

    /**
     * ログレベルを指定するコンストラクタ。
     * @param logLevel ログレベル
     */
    public LogCountMetrics(LogLevel logLevel) {
        this(DEFAULT_METRICS_NAME, DEFAULT_METRICS_DESCRIPTION, logLevel);
    }

    /**
     * ログレベルを指定するコンストラクタ。
     * @param metricsName メトリクス名
     * @param metricsDescription メトリクスの説明
     */
    public LogCountMetrics(String metricsName, String metricsDescription) {
        this(metricsName, metricsDescription, DEFAULT_LOG_LEVEL);
    }

    /**
     * ログレベルを指定するコンストラクタ。
     * @param metricsName メトリクス名
     * @param metricsDescription メトリクスの説明
     * @param logLevel ログレベル
     */
    public LogCountMetrics(String metricsName, String metricsDescription, LogLevel logLevel) {
        this.metricsName = metricsName;
        this.metricsDescription = metricsDescription;
        this.logLevel = logLevel;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        logListener = logContext -> {
            if (logLevel.getValue() < logContext.getLevel().getValue()) {
                return;
            }

            Counter.builder(metricsName)
                    .tag(TAG_NAME_LEVEL, logContext.getLevel().name())
                    .tag(TAG_NAME_RUNTIME_LOGGER, logContext.getRuntimeLoggerName())
                    .description(metricsDescription)
                    .register(registry)
                    .increment();
        };

        LogPublisher.addListener(logListener);
    }

    @Override
    public void close() {
        LogPublisher.removeListener(logListener);
    }
}
