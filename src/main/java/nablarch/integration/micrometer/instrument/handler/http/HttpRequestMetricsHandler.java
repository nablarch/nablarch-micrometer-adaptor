package nablarch.integration.micrometer.instrument.handler.http;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpRequestHandler;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.servlet.ServletExecutionContext;

import javax.servlet.http.HttpServletResponse;

/**
 * HTTPリクエストの処理時間をメトリクスとして収集するハンドラ。
 * <p>
 * メトリクスは、 {@code http.server.requests} という名前で作成される。<br>
 * また、メトリクスには以下のタグが設定される。
 * <table>
 *   <tr>
 *     <th>タグ</th>
 *     <th>説明</th>
 *   </tr>
 *   <tr>
 *     <td>class</td>
 *     <td>リクエストを処理したクラスの名前（取得できない場合は {@code UNKNOWN}）</td>
 *   </tr>
 *   <tr>
 *     <td>method</td>
 *     <td>リクエストを処理したメソッドの名前（取得できない場合は {@code UNKNOWN}）</td>
 *   </tr>
 *   <tr>
 *     <td>httpMethod</td>
 *     <td>HTTPメソッド</td>
 *   </tr>
 *   <tr>
 *     <td>status</td>
 *     <td>HTTPステータスコード</td>
 *   </tr>
 *   <tr>
 *     <td>outcome</td>
 *     <td>
 *       HTTPステータスコードの種類を表す文字列。<br>
 *       1XX は {@code INFORMATION}, 2XX は {@code SUCCESS}, 3XX は {@code REDIRECTION},
 *       4XX は {@code CLIENT_ERROR}, 5XX は {@code SERVER_ERROR}, それ以外の場合は {@code UNKNOWN}。
 *     </td>
 *   </tr>
 *   <tr>
 *     <td>exception</td>
 *     <td>例外がスローされた場合は、そのクラスの単純名（スローされていない場合は {@code "None"}）</td>
 *   </tr>
 * </table>
 * </p>
 *
 * @author Tanaka Tomoyuki
 */
public class HttpRequestMetricsHandler implements HttpRequestHandler {
    /**
     * アクションクラス名をリクエストスコープから取得するときのデフォルトのキー。
     */
    static final String DEFAULT_REQUEST_MAPPING_CLASS_VAR_NAME = "nablarch_request_mapping_class";
    /**
     * アクションクラスのメソッド名をリクエストスコープから取得するときのデフォルトのキー。
     */
    static final String DEFAULT_REQUEST_MAPPING_METHOD_VAR_NAME = "nablarch_request_mapping_method";

    private MeterRegistry meterRegistry;

    private String requestMappingClassVarName = DEFAULT_REQUEST_MAPPING_CLASS_VAR_NAME;
    private String requestMappingMethodVarName = DEFAULT_REQUEST_MAPPING_METHOD_VAR_NAME;

    @Override
    public HttpResponse handle(HttpRequest request, ExecutionContext context) {
        if (meterRegistry == null) {
            throw new IllegalStateException("meterRegistry is not set.");
        }

        Timer.Sample sample = Timer.start(meterRegistry);

        Throwable cachedThrowable = null;
        try {
            return context.handleNext(request);
        } catch (Throwable th) {
            cachedThrowable = th;
            throw th;
        } finally {
            Timer timer = buildTimer(request, context, cachedThrowable);
            sample.stop(timer);
        }
    }

    private Timer buildTimer(HttpRequest request, ExecutionContext context, Throwable cachedThrowable) {
        HttpServletResponse servletResponse = ((ServletExecutionContext) context).getServletResponse();
        Throwable throwable = cachedThrowable != null ? cachedThrowable : context.getException();

        String className = context.getRequestScopedVar(requestMappingClassVarName);
        String methodName = context.getRequestScopedVar(requestMappingMethodVarName);

        return Timer.builder("http.server.requests")
                .tag("class", className != null ? className : "UNKNOWN")
                .tag("method", methodName != null ? methodName : "UNKNOWN")
                .tag("httpMethod", request.getMethod())
                .tag("status", String.valueOf(servletResponse.getStatus()))
                .tag("outcome", resolveOutcome(servletResponse.getStatus()))
                .tag("exception", throwable == null ? "None" : throwable.getClass().getSimpleName())
                .register(meterRegistry);
    }

    private String resolveOutcome(int statusCode) {
        if (100 <= statusCode && statusCode < 200) {
            return "INFORMATION";
        } else if (200 <= statusCode && statusCode < 300) {
            return "SUCCESS";
        } else if (300 <= statusCode && statusCode < 400) {
            return "REDIRECTION";
        } else if (400 <= statusCode && statusCode < 500) {
            return "CLIENT_ERROR";
        } else if (500 <= statusCode && statusCode < 600) {
            return "SERVER_ERROR";
        } else {
            return "UNKNOWN";
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
     * アクションクラス名をリクエストスコープから取得するときのキーを指定する。
     * @param requestMappingClassVarName アクションクラス名のキー
     */
    public void setRequestMappingClassVarName(String requestMappingClassVarName) {
        this.requestMappingClassVarName = requestMappingClassVarName;
    }

    /**
     * アクションクラスのメソッド名をリクエストスコープから取得するときのキーを指定する。
     * @param requestMappingMethodVarName アクションクラスのメソッド名のキー
     */
    public void setRequestMappingMethodVarName(String requestMappingMethodVarName) {
        this.requestMappingMethodVarName = requestMappingMethodVarName;
    }
}
