package nablarch.integration.micrometer;

import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.repository.disposal.Disposable;
import nablarch.integration.micrometer.instrument.binder.jvm.NablarchGcCountMetrics;

import java.util.Arrays;
import java.util.List;

/**
 * デフォルトの{@link MeterBinder}リストを提供するクラス。
 * <p>
 * {@link MeterBinder}の中には{@link AutoCloseable}を実装したものがある（例：{@link JvmGcMetrics}）。<br>
 * このクラスは{@link Disposable}を実装しており、作成した{@link MeterBinder}の中に
 * {@link AutoCloseable}を実装したものがある場合は、{@code close()}を呼ぶようになっている。
 * </p>
 * <p>
 * {@link AutoCloseable}な{@link MeterBinder}を含むリストを返す独自の{@link MeterBinderListProvider}が必要な場合は、
 * このクラスを継承して{@link #createMeterBinderList()}をオーバーライドして作成することで
 * {@code close()}の実装を省略できる。
 * </p>
 * @author Tanaka Tomoyuki
 */
public class DefaultMeterBinderListProvider implements MeterBinderListProvider, Disposable {
    /** ロガー。 */
    private static final Logger LOGGER = LoggerManager.get(DefaultMeterBinderListProvider.class);

    /** 供給する {@link MeterBinder} のリスト。 */
    private final List<MeterBinder> meterBinderList;

    /**
     * コンストラクタ。
     */
    public DefaultMeterBinderListProvider() {
        meterBinderList = createMeterBinderList();
    }

    /**
     * {@link #provide()}で返す{@link MeterBinder}のリストを生成する。
     * @return {@link #provide()}で返す{@link MeterBinder}のリスト
     */
    protected List<MeterBinder> createMeterBinderList() {
        return Arrays.asList(
            new JvmMemoryMetrics(),
            new JvmGcMetrics(),
            new JvmThreadMetrics(),
            new ClassLoaderMetrics(),
            new ProcessorMetrics(),
            new FileDescriptorMetrics(),
            new UptimeMetrics(),
            new NablarchGcCountMetrics()
        );
    }

    @Override
    public List<MeterBinder> provide() {
        return meterBinderList;
    }

    @Override
    public void dispose() {
        for (MeterBinder meterBinder : meterBinderList) {
            if (meterBinder instanceof AutoCloseable) {
                try {
                    ((AutoCloseable)meterBinder).close();
                } catch (Exception e) {
                    LOGGER.logWarn("Failed to close MeterBinder(" + meterBinder + ")", e);
                }
            }
        }
    }
}
