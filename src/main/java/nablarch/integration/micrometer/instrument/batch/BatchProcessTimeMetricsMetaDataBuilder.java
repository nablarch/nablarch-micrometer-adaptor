package nablarch.integration.micrometer.instrument.batch;

import io.micrometer.core.instrument.Tag;
import nablarch.fw.ExecutionContext;
import nablarch.fw.launcher.CommandLine;
import nablarch.integration.micrometer.instrument.handler.HandlerMetricsMetaDataBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * バッチ処理時間メトリクス用のメタ情報を生成するクラス。
 * <p>
 * メトリクス名は{@code "batch.process.time"}を返す。
 * </p>
 * <p>
 * また、このクラスは以下のタグを生成する。
 * <ul>
 *   <li>{@code class} : バッチのアクションクラス名</li>
 *   <li>{@code status} : バッチの終了ステータス</li>
 * </ul>
 * </p>
 *
 * @author Tanaka Tomoyuki
 */
public class BatchProcessTimeMetricsMetaDataBuilder implements HandlerMetricsMetaDataBuilder<CommandLine, Object> {
    /** デフォルトのメトリクス名。 */
    static final String DEFAULT_METRICS_NAME = "batch.process.time";
    /** デフォルトのメトリクスの説明。 */
    static final String DEFAULT_METRICS_DESCRIPTION = "Batch process time.";

    /** メトリクス名。 */
    private String metricsName = DEFAULT_METRICS_NAME;
    /** メトリクスの説明。 */
    private String metricsDescription = DEFAULT_METRICS_DESCRIPTION;

    @Override
    public List<Tag> buildTagList(CommandLine commandLine, ExecutionContext executionContext, Object result, Throwable thrownThrowable) {
        return Arrays.asList(
            BatchActionClassTagUtil.obtain(commandLine.getRequestPath()),
            Tag.of("status", Objects.toString(result, "None"))
        );
    }

    @Override
    public String getMetricsName() {
        return metricsName;
    }

    @Override
    public String getMetricsDescription() {
        return metricsDescription;
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
}
