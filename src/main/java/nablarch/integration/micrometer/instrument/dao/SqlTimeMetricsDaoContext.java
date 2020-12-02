package nablarch.integration.micrometer.instrument.dao;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import nablarch.common.dao.DaoContext;
import nablarch.common.dao.EntityList;

import javax.persistence.OptimisticLockException;
import java.util.List;
import java.util.function.Supplier;

/**
 * SQLの処理実行時間をメトリクスとして計測する{@link DaoContext}のラッパークラス。
 * <p>
 * メトリクスは、{@code sql.process.time}という名前になる。
 * </p>
 * <p>
 * また、メトリクスには以下のタグが設定される。
 * <ul>
 *   <li>{@code sql.id}: SQLID(無い場合は{@code "None"})</li>
 *   <li>{@code entity}: エンティティクラスの名前({@link Class#getName()})。</li>
 *   <li>{@code method}: 実行された{@link DaoContext}のメソッドの単純名</li>
 * </ul>
 * </p>
 * <p>
 * 引数で渡されたエンティティまたはエンティティのリストが、{@code null}または空のリストの場合は、
 * 時間は計測されない（委譲先のメソッドの処理は実行される）。
 * </p>
 * @author Tanaka Tomoyuki
 */
public class SqlTimeMetricsDaoContext implements DaoContext {
    /**
     * デフォルトのメトリクス名。
     */
    static final String DEFAULT_METRICS_NAME = "sql.process.time";

    /**
     * デフォルトのメトリクスの説明。
     */
    static final String DEFAULT_METRICS_DESCRIPTION = "Time of processing sql.";

    /**
     * SQLIDのタグ名。
     */
    static final String TAG_NAME_SQL_ID = "sql.id";

    /**
     * エンティティ名のタグ名。
     */
    static final String TAG_NAME_ENTITY_NAME = "entity";

    /**
     * 実行されたメソッド名のタグ名。
     */
    static final String TAG_NAME_METHOD_NAME = "method";

    /**
     * SQLIDが無い場合に設定されるタグの値。
     */
    static final String TAG_VALUE_NO_SQL_ID = "None";

    private final DaoContext delegate;
    private final MeterRegistry meterRegistry;

    private String metricsName = DEFAULT_METRICS_NAME;
    private String metricsDescription = DEFAULT_METRICS_DESCRIPTION;

    /**
     * 委譲先の {@link DaoContext}と{@link MeterRegistry}を指定するコンストラクタ。
     * @param delegate 委譲先の{@link DaoContext}
     * @param meterRegistry {@link MeterRegistry}
     */
    public SqlTimeMetricsDaoContext(DaoContext delegate, MeterRegistry meterRegistry) {
        this.delegate = delegate;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public <T> T findById(Class<T> entityClass, Object... id) {
        return recordTime(TAG_VALUE_NO_SQL_ID, entityClass, "findById",
                () -> delegate.findById(entityClass, id));
    }

    @Override
    public <T> EntityList<T> findAll(Class<T> entityClass) {
        return recordTime(TAG_VALUE_NO_SQL_ID, entityClass, "findAll",
                () -> delegate.findAll(entityClass));
    }

    @Override
    public <T> EntityList<T> findAllBySqlFile(Class<T> entityClass, String sqlId, Object params) {
        return recordTime(sqlId, entityClass, "findAllBySqlFile",
                () -> delegate.findAllBySqlFile(entityClass, sqlId, params));
    }

    @Override
    public <T> EntityList<T> findAllBySqlFile(Class<T> entityClass, String sqlId) {
        return recordTime(sqlId, entityClass, "findAllBySqlFile",
                () -> delegate.findAllBySqlFile(entityClass, sqlId));
    }

    @Override
    public <T> T findBySqlFile(Class<T> entityClass, String sqlId, Object params) {
        return recordTime(sqlId, entityClass, "findBySqlFile",
                () -> delegate.findBySqlFile(entityClass, sqlId, params));
    }

    @Override
    public <T> long countBySqlFile(Class<T> entityClass, String sqlId, Object params) {
        return recordTime(sqlId, entityClass, "countBySqlFile",
                () -> delegate.countBySqlFile(entityClass, sqlId, params));
    }

    @Override
    public <T> int update(T entity) throws OptimisticLockException {
        return recordEntityUpdate(entity, "update", () -> delegate.update(entity));
    }

    @Override
    public <T> void batchUpdate(List<T> entities) {
        recordEntityListUpdate(entities, "batchUpdate", () -> delegate.batchUpdate(entities));
    }

    @Override
    public <T> void insert(T entity) {
        recordEntityUpdate(entity, "insert", () -> {
            delegate.insert(entity);
            return null;
        });
    }

    @Override
    public <T> void batchInsert(List<T> entities) {
        recordEntityListUpdate(entities, "batchInsert", () -> delegate.batchInsert(entities));
    }

    @Override
    public <T> int delete(T entity) {
        return recordEntityUpdate(entity, "delete", () -> delegate.delete(entity));
    }

    @Override
    public <T> void batchDelete(List<T> entities) {
        recordEntityListUpdate(entities, "batchDelete", () -> delegate.batchDelete(entities));
    }

    private void recordEntityListUpdate(List<?> entities, String methodName, Runnable execution) {
        if (entities == null || entities.isEmpty()) {
            execution.run();
            return;
        }

        recordTime(TAG_VALUE_NO_SQL_ID, entities.get(0).getClass(), methodName, () -> {
            execution.run();
            return null;
        });
    }

    private <T> T recordEntityUpdate(Object entity, String methodName, Supplier<T> execution) {
        if (entity == null) {
            return execution.get();
        }

        return recordTime(TAG_VALUE_NO_SQL_ID, entity.getClass(), methodName, execution);
    }

    private <T> T recordTime(String sqlId, Class<?> entityClass, String methodName, Supplier<T> execution) {
        return Timer.builder(metricsName)
                    .description(metricsDescription)
                    .tag(TAG_NAME_SQL_ID, sqlId)
                    .tag(TAG_NAME_ENTITY_NAME, entityClass.getName())
                    .tag(TAG_NAME_METHOD_NAME, methodName)
                    .register(meterRegistry)
                    .record(execution);
    }

    @Override
    public DaoContext page(long page) {
        delegate.page(page);
        return this;
    }

    @Override
    public DaoContext per(long per) {
        delegate.per(per);
        return this;
    }

    @Override
    public DaoContext defer() {
        delegate.defer();
        return this;
    }

    /**
     * メトリクスの名前を設定する。
     * @param metricsName メトリクスの名前
     */
    public void setMetricsName(String metricsName) {
        this.metricsName = metricsName;
    }

    /**
     * メトリクス名を取得する。
     * @return メトリクス名
     */
    public String getMetricsName() {
        return metricsName;
    }

    /**
     * メトリクスの説明を設定する。
     * @param metricsDescription メトリクスの説明
     */
    public void setMetricsDescription(String metricsDescription) {
        this.metricsDescription = metricsDescription;
    }

    /**
     * メトリクスの説明を取得する。
     * @return メトリクスの説明
     */
    public String getMetricsDescription() {
        return metricsDescription;
    }

    /**
     * 委譲先の{@link DaoContext}を取得する。
     * @return 委譲先の {@link DaoContext}
     */
    public DaoContext getDelegate() {
        return delegate;
    }

    /**
     * {@link MeterRegistry}を取得する。
     * @return {@link MeterRegistry}
     */
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }
}
