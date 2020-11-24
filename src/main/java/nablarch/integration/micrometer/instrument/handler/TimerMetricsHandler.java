package nablarch.integration.micrometer.instrument.handler;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;

import java.util.List;

/**
 * ハンドラキューに追加することで、後続処理の実行時間をメトリクスとして収集するハンドラクラス。
 * @param <TData> 処理対象データ型
 * @param <TResult> 処理結果データ型
 * @author Tanaka Tomoyuki
 */
public class TimerMetricsHandler<TData, TResult> implements Handler<TData, TResult> {
    private MeterRegistry meterRegistry;
    private HandlerMetricsMetaDataBuilder<TData, TResult> handlerMetricsMetaDataBuilder;

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

            Timer timer = Timer.builder(handlerMetricsMetaDataBuilder.getMetricsName())
                    .description(handlerMetricsMetaDataBuilder.getMetricsDescription())
                    .tags(tagList)
                    .register(meterRegistry);

            sample.stop(timer);
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
}
