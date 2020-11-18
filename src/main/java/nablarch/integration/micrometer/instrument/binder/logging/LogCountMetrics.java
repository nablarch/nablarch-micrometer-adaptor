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
     * メトリクスの名前。
     */
    static final String METRICS_NAME = "log.count";

    /**
     * ログレベルのタグ名。
     */
    static final String TAG_NAME_LEVEL = "level";

    /**
     * 実行時ロガー名のタグ名。
     */
    static final String TAG_NAME_RUNTIME_LOGGER = "logger";

    private LogLevel logLevel = LogLevel.WARN;
    private LogListener logListener;

    @Override
    public void bindTo(MeterRegistry registry) {
        logListener = logContext -> {
            if (logLevel.getValue() < logContext.getLevel().getValue()) {
                return;
            }

            Counter.builder(METRICS_NAME)
                    .tag(TAG_NAME_LEVEL, logContext.getLevel().name())
                    .tag(TAG_NAME_RUNTIME_LOGGER, logContext.getRuntimeLoggerName())
                    .description("logging count for each log level and runtime logger.")
                    .register(registry)
                    .increment();
        };

        LogPublisher.addListener(logListener);
    }

    /**
     * カウント対象となるログレベルの下限値を設定する。
     * <p>
     * ここで設定したログレベル以上のログだけがカウントの対象となる。<br>
     * デフォルトは {@link LogLevel#WARN}。
     * </p>
     * @param logLevel カウント対象となるログレベルの下限値
     */
    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    public void close() {
        LogPublisher.removeListener(logListener);
    }
}
