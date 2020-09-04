package nablarch.integration.micrometer.instrument.binder.jvm;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.List;

/**
 * GCの発生回数をカウントする{@link MeterBinder}。
 * <p>
 * {@code jvm.gc.count} という名前のメトリクスが、メモリマネージャの数だけ登録される。<br>
 * 各メトリクスには、メモリマネージャの名前が {@code memory.manager.name} タグで設定される。
 * </p>
 * @author Tanaka Tomoyuki
 */
public class NablarchGcCountMetrics implements MeterBinder {

    private final Iterable<Tag> tags;

    /**
     * コンストラクタ。
     */
    public NablarchGcCountMetrics() {
        this(Collections.emptyList());
    }

    /**
     * 追加のタグを指定するコンストラクタ。
     * @param tags 追加で指定するタグ
     */
    public NablarchGcCountMetrics(Iterable<Tag> tags) {
        this.tags = tags;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();

        for (GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMXBeans) {
            FunctionCounter.builder("jvm.gc.count", garbageCollectorMXBean, GarbageCollectorMXBean::getCollectionCount)
                    .tag("memory.manager.name", garbageCollectorMXBean.getName())
                    .tags(tags)
                    .description("Count of garbage collection")
                    .register(registry);
        }
    }
}
