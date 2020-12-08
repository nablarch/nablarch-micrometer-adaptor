package nablarch.integration.micrometer.instrument.binder.jmx;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
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
public class JmxGaugeMetrics implements MeterBinder {
    private final MetricsMetaData metricsMetaData;
    private final MBeanAttributeCondition condition;

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
