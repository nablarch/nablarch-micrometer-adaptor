package nablarch.integration.micrometer.instrument.handler.http;

import io.micrometer.core.instrument.Tag;
import nablarch.fw.ExecutionContext;
import nablarch.fw.web.HttpRequest;

import java.util.List;

/**
 * {@link HttpRequestMetricsHandler}でメトリクスに設定するタグのリスト生成機能を提供するインタフェース。
 * @author Tanaka Tomoyuki
 */
public interface HttpRequestMetricsTagBuilder {

    /**
     * メトリクスに設定するタグのリストを生成する。
     * @param request HTTPリクエスト
     * @param context 実行時のコンテキスト
     * @param thrownThrowable 後続ハンドラによってスローされた例外(例外がスローされていない場合は {@code null})
     * @return 生成したタグのリスト
     */
    List<Tag> build(HttpRequest request, ExecutionContext context, Throwable thrownThrowable);
}
