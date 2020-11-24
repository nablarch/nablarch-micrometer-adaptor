package nablarch.integration.micrometer.instrument.batch;

import io.micrometer.core.instrument.Tag;

/**
 * バッチのリクエストパスからアクションクラスの情報をタグとして取り出すためのユーティリティ。
 * @author Tanaka Tomoyuki
 */
public final class BatchActionClassTagUtil {

    /**
     * リクエストパスからバッチアクションクラスの名前を取得して {@link Tag} で返す。
     * @param requestPath リクエストパス
     * @return バッチアクションクラス名を設定した {@link Tag}
     */
    public static Tag obtain(String requestPath) {
        String[] tokens = requestPath.split("/");
        if (tokens.length < 2) {
            throw new IllegalArgumentException("illegal requestPath format. requestPath='" + requestPath + "'.");
        }
        return Tag.of("class", tokens[0]);
    }

    private BatchActionClassTagUtil() {}
}
