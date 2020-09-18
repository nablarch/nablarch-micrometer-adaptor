package nablarch.integration.micrometer.instrument.handler.http;

import io.micrometer.core.instrument.Tag;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.servlet.ServletExecutionContext;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link HttpRequestMetricsTagBuilder}のデフォルト実装。
 * <p>
 * タグのリストは以下の内容で作成する。
 * <table>
 *   <tr>
 *     <th>タグ</th>
 *     <th>説明</th>
 *   </tr>
 *   <tr>
 *     <td>class</td>
 *     <td>リクエストを処理したクラスの名前（HTTPステータスが {@code 404} 、または取得できない場合は {@code UNKNOWN}）</td>
 *   </tr>
 *   <tr>
 *     <td>method</td>
 *     <td>リクエストを処理したメソッドの名前（HTTPステータスが {@code 404} 、取得できない場合は {@code UNKNOWN}）</td>
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
 */
public class DefaultHttpRequestMetricsTagBuilder implements HttpRequestMetricsTagBuilder {
    /**
     * アクションクラス名をリクエストスコープから取得するときのデフォルトのキー。
     */
    static final String DEFAULT_REQUEST_MAPPING_CLASS_VAR_NAME = "nablarch_request_mapping_class";
    /**
     * アクションクラスのメソッド名をリクエストスコープから取得するときのデフォルトのキー。
     */
    static final String DEFAULT_REQUEST_MAPPING_METHOD_VAR_NAME = "nablarch_request_mapping_method";

    private String requestMappingClassVarName = DEFAULT_REQUEST_MAPPING_CLASS_VAR_NAME;
    private String requestMappingMethodVarName = DEFAULT_REQUEST_MAPPING_METHOD_VAR_NAME;

    @Override
    public List<Tag> build(HttpRequest request, ExecutionContext context, Throwable thrownThrowable) {
        List<Tag> tagList = new ArrayList<>();

        HttpServletResponse servletResponse = ((ServletExecutionContext) context).getServletResponse();

        tagList.add(Tag.of("httpMethod", request.getMethod()));
        tagList.add(Tag.of("status", String.valueOf(servletResponse.getStatus())));
        tagList.add(Tag.of("outcome", resolveOutcome(servletResponse.getStatus())));
        tagList.add(Tag.of("exception", resolveException(context, thrownThrowable)));

        if (servletResponse.getStatus() == HttpServletResponse.SC_NOT_FOUND) {
            tagList.add(Tag.of("class", "UNKNOWN"));
            tagList.add(Tag.of("method", "UNKNOWN"));
        } else {
            String className = context.getRequestScopedVar(requestMappingClassVarName);
            String methodName = context.getRequestScopedVar(requestMappingMethodVarName);

            tagList.add(Tag.of("class", className != null ? className : "UNKNOWN"));
            tagList.add(Tag.of("method", methodName != null ? methodName : "UNKNOWN"));
        }

        return tagList;
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

    private String resolveException(ExecutionContext context, Throwable cachedThrowable) {
        Throwable throwable = cachedThrowable != null ? cachedThrowable : context.getException();
        return throwable == null ? "None" : throwable.getClass().getSimpleName();
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
