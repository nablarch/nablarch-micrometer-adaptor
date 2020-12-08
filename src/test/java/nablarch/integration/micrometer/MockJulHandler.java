package nablarch.integration.micrometer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Micrometerが内部で出力したログ出力を収集してテストできるようにするためのハンドラ。
 * <p>
 * 以下のようにルートロガーに追加することで、JULに出力されたログの情報が収集できるようになる。
 * <pre>{@code
 * Logger rootLogger = Logger.getLogger("");
 * MockJulHandler mockJulHandler = new MockJulHandler();
 * rootLogger.addHandler(mockJulHandler);
 * ...
 * rootLogger.removeHandler(mockJulHandler); // teardown などで削除する
 * }</pre>
 * </p>
 * <p>
 * 収集されたログの情報は、{@link #getPublishedRecordList()}で取得できる。
 * </p>
 *
 * @author Tanaka Tomoyuki
 */
public class MockJulHandler extends Handler {
    private List<LogRecord> publishedRecordList = new ArrayList<>();

    @Override
    public void publish(LogRecord record) {
        publishedRecordList.add(record);
    }

    /**
     * 出力された{@link LogRecord}のリストを取得する。
     * @return 出力された{@link LogRecord}のリスト
     */
    public List<LogRecord> getPublishedRecordList() {
        return publishedRecordList;
    }

    @Override public void flush() {}

    @Override public void close() throws SecurityException {}
}
