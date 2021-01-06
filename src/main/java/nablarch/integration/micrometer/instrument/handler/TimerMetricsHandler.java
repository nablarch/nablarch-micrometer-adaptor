package nablarch.integration.micrometer.instrument.handler;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ハンドラキューに追加することで、後続処理の実行時間をメトリクスとして収集するハンドラクラス。
 * @param <TData> 処理対象データ型
 * @param <TResult> 処理結果データ型
 * @author Tanaka Tomoyuki
 */
public class TimerMetricsHandler<TData, TResult> implements Handler<TData, TResult> {
    /** 使用する{@link MeterRegistry}。 */
    private MeterRegistry meterRegistry;
    /** {@link HandlerMetricsMetaDataBuilder}。 */
    private HandlerMetricsMetaDataBuilder<TData, TResult> handlerMetricsMetaDataBuilder;

    /** 収集対象のパーセンタイル。 */
    private double[] percentiles;
    /** ヒストグラムバケットの連携の有効・無効フラグ。 */
    private boolean enablePercentileHistogram;
    /** SLOに基づく追加のバケット。 */
    private Duration[] serviceLevelObjectives;
    /** バケットの最小値。 */
    private Long minimumExpectedValue;
    /** バケットの最大値。 */
    private Long maximumExpectedValue;

    @Override
    public TResult handle(TData param, ExecutionContext executionContext) {
        if (meterRegistry == null) {
            throw new IllegalStateException("meterRegistry is not set.");
        }
        if (handlerMetricsMetaDataBuilder == null) {
            throw new IllegalStateException("handlerMetricsMetaDataBuilder is not set.");
        }

        TResult result = null;
        Throwable thrownThrowable = null;
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            result = executionContext.handleNext(param);
            return result;
        } catch (Throwable throwable) {
            thrownThrowable = throwable;
            throw throwable;
        } finally {
            List<Tag> tagList = handlerMetricsMetaDataBuilder.buildTagList(param, executionContext, result, thrownThrowable);

            Timer.Builder builder = Timer.builder(handlerMetricsMetaDataBuilder.getMetricsName());

            setupPercentileOptions(builder);

            Timer timer = builder
                    .description(handlerMetricsMetaDataBuilder.getMetricsDescription())
                    .tags(tagList)
                    .register(meterRegistry);

            sample.stop(timer);
        }
    }

    /**
     * パーセンタイルについての設定を行う。
     * @param builder 設定対象の{@link io.micrometer.core.instrument.Timer.Builder}
     */
    private void setupPercentileOptions(Timer.Builder builder) {
        builder.publishPercentileHistogram(enablePercentileHistogram);

        if (percentiles != null) {
            builder.publishPercentiles(percentiles);
        }
        if (serviceLevelObjectives != null) {
            builder.serviceLevelObjectives(serviceLevelObjectives);
        }
        if (minimumExpectedValue != null) {
            builder.minimumExpectedValue(Duration.ofMillis(minimumExpectedValue));
        }
        if (maximumExpectedValue != null) {
            builder.maximumExpectedValue(Duration.ofMillis(maximumExpectedValue));
        }
    }

    /**
     * {@link MeterRegistry} を設定する。
     * @param meterRegistry {@link MeterRegistry}
     */
    public void setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * {@link HandlerMetricsMetaDataBuilder} を設定する。
     * @param handlerMetricsMetaDataBuilder {@link HandlerMetricsMetaDataBuilder}
     */
    public void setHandlerMetricsMetaDataBuilder(HandlerMetricsMetaDataBuilder<TData, TResult> handlerMetricsMetaDataBuilder) {
        this.handlerMetricsMetaDataBuilder = handlerMetricsMetaDataBuilder;
    }

    /**
     * このハンドラによって収集されるメトリクスに、指定されたパーセンタイルのメトリクスを追加する。
     * <p>
     * 95パーセンタイルの情報を追加したい場合は、{@code 0.95}を設定する。
     * </p>
     * <p>
     * このセッターはコンポーネント定義ファイルからプロパティとして設定されることを想定している。<br>
     * システムリポジトリによるリストプロパティの設定は総称型に応じたキャストをサポートしていないため、
     * いったん文字列で受け取って内部で{@code double}にパースしている。
     * </p>
     * <p>
     * ここで渡した値は、{@code io.micrometer.core.instrument.Timer.Builder#publishPercentiles(double...)}の引数に渡される。
     * </p>
     * @param percentiles 追加するパーセンタイルのリスト
     */
    public void setPercentiles(List<String> percentiles) {
        this.percentiles = percentiles.stream().mapToDouble(Double::parseDouble).toArray();
    }

    /**
     * ヒストグラムバケットを生成するかどうかを設定する。
     * <p>
     * ここで渡した値は、{@code io.micrometer.core.instrument.Timer.Builder#publishPercentileHistogram(java.lang.Boolean)}の引数に渡される。
     * </p>
     * @param enablePercentileHistogram ヒストグラムバケットを生成する場合は{@code true}
     */
    public void setEnablePercentileHistogram(boolean enablePercentileHistogram) {
        this.enablePercentileHistogram = enablePercentileHistogram;
    }

    /**
     * サービスレベル目標（ミリ秒）のリストを設定する。
     * <p>
     * このセッターはコンポーネント定義ファイルからプロパティとして設定されることを想定している。<br>
     * システムリポジトリによるリストプロパティの設定は総称型に応じたキャストをサポートしていないため、
     * いったん文字列で受け取って内部で{@code long}にパースしている。
     * </p>
     * <p>
     * ここで渡した値は、{@code io.micrometer.core.instrument.Timer.Builder#serviceLevelObjectives(java.time.Duration...)}の引数に渡される。
     * </p>
     * @param serviceLevelObjectives サービスレベル目標のリスト
     */
    public void setServiceLevelObjectives(List<String> serviceLevelObjectives) {
        this.serviceLevelObjectives = serviceLevelObjectives.stream()
                                        .map(Long::parseLong)
                                        .map(Duration::ofMillis)
                                        .toArray(Duration[]::new);;
    }

    /**
     * ヒストグラムバケットの下限（ミリ秒）を設定する。
     * <p>
     * ここで渡した値は、{@code io.micrometer.core.instrument.Timer.Builder#minimumExpectedValue(java.time.Duration)}の引数に渡される。
     * </p>
     * @param minimumExpectedValue ヒストグラムバケットの下限
     */
    public void setMinimumExpectedValue(long minimumExpectedValue) {
        this.minimumExpectedValue = minimumExpectedValue;
    }

    /**
     * ヒストグラムバケットの上限（ミリ秒）を設定する。
     * <p>
     * ここで渡した値は、{@code io.micrometer.core.instrument.Timer.Builder#maximumExpectedValue(java.time.Duration)}の引数に渡される。
     * </p>
     * @param maximumExpectedValue ヒストグラムバケットの上限
     */
    public void setMaximumExpectedValue(long maximumExpectedValue) {
        this.maximumExpectedValue = maximumExpectedValue;
    }
}
