package nablarch.integration.micrometer.instrument.binder;

import io.micrometer.core.instrument.Tag;

import java.util.Collections;

/**
 * メトリクスに設定する情報（名前、説明、タグ）を保持するデータクラス。
 *
 * @author Tanaka Tomoyuki
 */
public class MetricsMetaData {
    /** メトリクス名。 */
    private final String name;
    /** メトリクスの説明。 */
    private final String description;
    /** タグの一覧。 */
    private final Iterable<Tag> tags;

    /**
     * 名前、説明、タグ一覧を指定するコンストラクタ。
     * @param name メトリクスの名前
     * @param description メトリクスの説明
     * @param tags メトリクスに設定するタグ一覧
     */
    public MetricsMetaData(String name, String description, Iterable<Tag> tags) {
        this.name = name;
        this.description = description;
        this.tags = tags;
    }

    /**
     * 名前、説明を指定するコンストラクタ。
     * @param name メトリクスの名前
     * @param description メトリクスの説明
     */
    public MetricsMetaData(String name, String description) {
        this(name, description, Collections.emptyList());
    }

    /**
     * メトリクスの名前を取得する。
     * @return メトリクスの名前
     */
    public String getName() {
        return name;
    }

    /**
     * メトリクスの説明を取得する。
     * @return メトリクスの説明
     */
    public String getDescription() {
        return description;
    }

    /**
     * タグ一覧を取得する。
     * @return タグ一覧
     */
    public Iterable<Tag> getTags() {
        return tags;
    }
}
