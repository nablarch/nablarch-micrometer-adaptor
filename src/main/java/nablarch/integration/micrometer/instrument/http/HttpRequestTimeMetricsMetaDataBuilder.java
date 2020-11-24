package nablarch.integration.micrometer.instrument.http;

import io.micrometer.core.instrument.Tag;
import nablarch.fw.ExecutionContext;
import nablarch.fw.handler.MethodBinding;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.servlet.ServletExecutionContext;
import nablarch.integration.micrometer.instrument.handler.HandlerMetricsMetaDataBuilder;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * HTTPリクエストの処理時間のメトリクスに設定するメタ情報を構築するビルダー。
 * <p>
 * メトリクスの名前は{@code "http.server.requests"}を返す。
 * </p>
 * <p>
 * また、タグのリストは以下の内容で作成する。
 * <table>
 *   <tr>
 *     <th>タグ</th>
 *     <th>説明</th>
 *   </tr>
 *   <tr>
 *     <td>class</td>
 *     <td>
 *       リクエストを処理したクラスの名前({@link Class#getName()})。<br>
 *       クラスの情報を取得できない場合は {@code UNKNOWN}。
 *     </td>
 *   </tr>
 *   <tr>
 *     <td>method</td>
 *     <td>
 *       リクエストを処理したメソッドを表す文字列。<br>
 *       この文字列は、メソッド名の後ろに引数の型の正規名({@link Class#getCanonicalName()})をアンダースコア({@code _})で
 *       つなげたものになる（例:{@code fooMethod_int_java.lang.String}）。<br>
 *       メソッドの情報を取得できない場合は {@code UNKNOWN}。
 *     </td>
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
 * @author Tanaka Tomoyuki
 */
public class HttpRequestTimeMetricsMetaDataBuilder implements HandlerMetricsMetaDataBuilder<HttpRequest, Object> {
    /**
     * デフォルトのメトリクス名。
     */
    static final String DEFAULT_METRICS_NAME = "http.server.requests";

    /**
     * デフォルトのメトリクスの説明。
     */
    static final String DEFAULT_METRICS_DESCRIPTION = "HTTP request metrics measured by Timer.";

    private String metricsName = DEFAULT_METRICS_NAME;
    private String metricsDescription = DEFAULT_METRICS_DESCRIPTION;

    @Override
    public List<Tag> buildTagList(HttpRequest request, ExecutionContext context, Object result, Throwable thrownThrowable) {
        Class<?> clazz = context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS);
        Method method = context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_METHOD);
        HttpServletResponse servletResponse = ((ServletExecutionContext) context).getServletResponse();

        /*
         * タグ仕様の背景説明
         *
         * ここで設定しているタグは、 Spring Boot Actuator の Spring MVC Metrics が設定しているタグを参考にしている。
         * ただし、 Actuator は uri タグを設定しているのに対して、本メトリクスでは class, method を設定している違いがある。
         * Actuator の uri は、リクエストを受けたパスの定義(/project/{id})が設定される。
         * しかし、このパス定義に該当する情報は、 Nablarch が利用している HTTP Request Router から取得できない。
         * したがって、本メトリクスでは uri の代わりに実行されたアクションクラスとメソッドの情報を出力している。
         * 参考: https://spring.pleiades.io/spring-boot/docs/2.3.4.RELEASE/reference/html/production-ready-features.html#production-ready-metrics-spring-mvc
         *
         * class, method タグの出力書式については、 Eclipse Microprofile の Metrics の仕様を参考にしている。
         * また、 class が Class#getName() で method の引数の型が Class#getCanonicalName() である点については、
         * Microprofile の Metrics を実装している Open Liberty の実際の動作を参考にしている。
         * 参考: https://download.eclipse.org/microprofile/microprofile-metrics-2.3/microprofile-metrics-spec-2.3.html#_optional_rest
         */
        return Arrays.asList(
            Tag.of("class", clazz != null ? clazz.getName() : "UNKNOWN"),
            Tag.of("method", method != null ? buildMethodTag(method) : "UNKNOWN"),
            Tag.of("httpMethod", request.getMethod()),
            Tag.of("status", String.valueOf(servletResponse.getStatus())),
            Tag.of("outcome", resolveOutcome(servletResponse.getStatus())),
            Tag.of("exception", resolveException(context, thrownThrowable))
        );
    }

    private String buildMethodTag(Method method) {
        StringBuilder sb = new StringBuilder(method.getName());

        for (Class<?> parameterType : method.getParameterTypes()) {
            sb.append("_").append(parameterType.getCanonicalName());
        }

        return sb.toString();
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

    private String resolveException(ExecutionContext context, Throwable thrownThrowable) {
        Throwable throwable = thrownThrowable != null ? thrownThrowable : context.getException();
        return throwable == null ? "None" : throwable.getClass().getSimpleName();
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
