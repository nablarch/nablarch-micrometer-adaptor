package nablarch.integration.micrometer.instrument.binder.jmx;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import nablarch.core.util.annotation.Published;
import nablarch.integration.micrometer.instrument.binder.MetricsMetaData;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * 指定したMBeanから定期的に値を取得し{@link Gauge}として記録する{@link MeterBinder}の実装クラス。
 *
 * @author Tanaka Tomoyuki
 */
@Published(tag = "architect")
public class JmxGaugeMetrics implements MeterBinder {
    /** メトリクスのメタ情報。 */
    private final MetricsMetaData metricsMetaData;
    /** 対象のMBeanを特定するための条件。 */
    private final MBeanAttributeCondition condition;

    /**
     * コンストラクタ。
     * @param metricsMetaData メトリクスのメタ情報
     * @param condition 対象のMBeanを特定するための条件
     */
    public JmxGaugeMetrics(MetricsMetaData metricsMetaData, MBeanAttributeCondition condition) {
        this.metricsMetaData = metricsMetaData;
        this.condition = condition;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder(metricsMetaData.getName(), this::obtainGaugeValue)
            .description(metricsMetaData.getDescription())
            .tags(metricsMetaData.getTags())
            .register(registry);
    }

    /**
     * {@link Gauge} に設定する値をMBeanから取得する。
     * <p>
     * MBeanが取得できない場合、または取得した値が{@link Number}でない場合は {@code NaN} を返す。
     * </p>
     * @return MBeanから取得した値
     */
    private double obtainGaugeValue() {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            Object value = server.getAttribute(new ObjectName(this.condition.getObjectName()), condition.getAttribute());
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else {
                return Double.NaN;
            }
        } catch (JMException e) {
            throw new RuntimeException(e);
        }
    }
}
