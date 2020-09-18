package nablarch.integration.micrometer.instrument.handler.http;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpRequestHandler;
import nablarch.fw.web.HttpResponse;

import java.util.List;

/**
 * HTTPリクエストの処理時間をメトリクスとして収集するハンドラ。
 * <p>
 * メトリクスは、 {@code http.server.requests} という名前で作成される。
 * </p>
 *
 * @author Tanaka Tomoyuki
 */
public class HttpRequestMetricsHandler implements HttpRequestHandler {

    private MeterRegistry meterRegistry;
    private HttpRequestMetricsTagBuilder httpRequestMetricsTagBuilder = new DefaultHttpRequestMetricsTagBuilder();

    @Override
    public HttpResponse handle(HttpRequest request, ExecutionContext context) {
        if (meterRegistry == null) {
            throw new IllegalStateException("meterRegistry is not set.");
        }

        Timer.Sample sample = Timer.start(meterRegistry);

        Throwable thrownThrowable = null;
        try {
            return context.handleNext(request);
        } catch (Throwable th) {
            thrownThrowable = th;
            throw th;
        } finally {
            List<Tag> tagList = httpRequestMetricsTagBuilder.build(request, context, thrownThrowable);
            Timer timer = Timer.builder("http.server.requests").tags(tagList).register(meterRegistry);
            sample.stop(timer);
        }
    }

    /**
     * {@link MeterRegistry}を設定する。
     * @param meterRegistry {@link MeterRegistry}
     */
    public void setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * {@link HttpRequestMetricsTagBuilder}を設定する。
     * @param httpRequestMetricsTagBuilder {@link HttpRequestMetricsTagBuilder}
     */
    public void setHttpRequestMetricsTagBuilder(HttpRequestMetricsTagBuilder httpRequestMetricsTagBuilder) {
        this.httpRequestMetricsTagBuilder = httpRequestMetricsTagBuilder;
    }
}
