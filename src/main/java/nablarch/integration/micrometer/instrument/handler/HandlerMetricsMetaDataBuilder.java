package nablarch.integration.micrometer.instrument.handler;

import io.micrometer.core.instrument.Tag;
import nablarch.fw.ExecutionContext;

import java.util.List;

/**
 * ハンドラで収集するメトリクスに設定するメタ情報を生成するビルダー。
 * @param <TData> 処理対象データ型
 * @param <TResult> 処理結果データ型
 * @author Tanaka Tomoyuki
 */
public interface HandlerMetricsMetaDataBuilder<TData, TResult> {

    /**
     * メトリクスの名前を取得する。
     * @return メトリクスの名前
     */
    String getMetricsName();

    /**
     * メトリクスの説明を取得する。
     * @return メトリクスの説明
     */
    String getMetricsDescription();

    /**
     * メトリクスに設定するタグのリストを生成する。
     * @param param ハンドラに渡された処理対象データ
     * @param executionContext 実行時コンテキスト
     * @param result ハンドラが返した処理結果データ（ハンドラが例外をスローした場合は {@code null}）
     * @param thrownThrowable ハンドラがスローした例外（例外がスローされていない場合は {@code null}）
     * @return 生成したタグのリスト
     */
    List<Tag> buildTagList(TData param, ExecutionContext executionContext, TResult result, Throwable thrownThrowable);
}
