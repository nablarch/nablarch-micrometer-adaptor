package nablarch.integration.micrometer.instrument.dao;

import io.micrometer.core.instrument.MeterRegistry;
import nablarch.common.dao.DaoContext;
import nablarch.common.dao.DaoContextFactory;

/**
 * 委譲対象({@code delegate})の{@link DaoContextFactory}が生成する{@link DaoContext}をラップした
 * {@link SqlTimeMetricsDaoContext}を生成するファクトリクラス。
 * @author Tanaka Tomoyuki
 */
public class SqlTimeMetricsDaoContextFactory extends DaoContextFactory {
    /** 移譲先の{@link DaoContextFactory}。 */
    private DaoContextFactory delegate;
    /** 使用する{@link MeterRegistry}。 */
    private MeterRegistry meterRegistry;
    /** メトリクス名。 */
    private String metricsName;
    /** メトリクスの説明。 */
    private String metricsDescription;

    @Override
    public DaoContext create() {
        if (delegate == null) {
            throw new IllegalStateException("delegate is null.");
        }
        if (meterRegistry == null) {
            throw new IllegalStateException("meterRegistry is null.");
        }

        SqlTimeMetricsDaoContext daoContext = new SqlTimeMetricsDaoContext(delegate.create(), meterRegistry);

        if (metricsName != null) {
            daoContext.setMetricsName(metricsName);
        }
        if (metricsDescription != null) {
            daoContext.setMetricsDescription(metricsDescription);
        }

        return daoContext;
    }

    /**
     * 委譲対象の{@link DaoContextFactory}を設定する。
     * @param delegate 委譲対象の{@link DaoContextFactory}
     */
    public void setDelegate(DaoContextFactory delegate) {
        this.delegate = delegate;
    }

    /**
     * {@link MeterRegistry}を設定する。
     * @param meterRegistry {@link MeterRegistry}
     */
    public void setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * {@link SqlTimeMetricsDaoContext}に設定するメトリクス名を指定する。
     * @param metricsName メトリクス名
     */
    public void setMetricsName(String metricsName) {
        this.metricsName = metricsName;
    }

    /**
     * {@link SqlTimeMetricsDaoContext}に設定するメトリクスの説明を指定する。
     * @param metricsDescription メトリクスの説明
     */
    public void setMetricsDescription(String metricsDescription) {
        this.metricsDescription = metricsDescription;
    }
}
