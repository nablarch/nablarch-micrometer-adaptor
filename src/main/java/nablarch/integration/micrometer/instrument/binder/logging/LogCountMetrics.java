package nablarch.integration.micrometer.instrument.binder.logging;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import nablarch.core.log.basic.LogLevel;
import nablarch.core.log.basic.LogListener;
import nablarch.core.log.basic.LogPublisher;
import nablarch.integration.micrometer.instrument.binder.MetricsMetaData;

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

    private final LogLevel thresholdOfLogLevel;
    private final MetricsMetaData metricsMetaData;
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
     * 収集するログレベルのしきい値を指定するコンストラクタ。
     * <p>
     * 指定されたログレベル以上のログ出力が計測の対象となる。
     * </p>
     *
     * @param thresholdOfLogLevel ログレベルのしきい値
     */
    public LogCountMetrics(LogLevel thresholdOfLogLevel) {
        this(new MetricsMetaData(DEFAULT_METRICS_NAME, DEFAULT_METRICS_DESCRIPTION), thresholdOfLogLevel);
    }

    /**
     * メトリクスの設定情報を指定するコンストラクタ。
     * @param metricsMetaData メトリクスの設定情報
     */
    public LogCountMetrics(MetricsMetaData metricsMetaData) {
        this(metricsMetaData, DEFAULT_LOG_LEVEL);
    }

    /**
     * メトリクスの設定情報と、収集するログレベルのしきい値を指定するコンストラクタ。
     * <p>
     * 指定されたログレベル以上のログ出力が計測の対象となる。
     * </p>
     *
     * @param metricsMetaData メトリクスの設定情報
     * @param thresholdOfLogLevel ログレベルのしきい値
     */
    public LogCountMetrics(MetricsMetaData metricsMetaData, LogLevel thresholdOfLogLevel) {
        this.metricsMetaData = metricsMetaData;
        this.thresholdOfLogLevel = thresholdOfLogLevel;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        logListener = logContext -> {
            if (thresholdOfLogLevel.getValue() < logContext.getLevel().getValue()) {
                return;
            }

            Counter.builder(metricsMetaData.getName())
                    .tag(TAG_NAME_LEVEL, logContext.getLevel().name())
                    .tag(TAG_NAME_RUNTIME_LOGGER, logContext.getRuntimeLoggerName())
                    .tags(metricsMetaData.getTags())
                    .description(metricsMetaData.getDescription())
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
