package nablarch.integration.micrometer.instrument.dao;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import mockit.Expectations;
import mockit.Mock;
import mockit.Mocked;
import nablarch.common.dao.DaoContext;
import nablarch.common.dao.DaoContextFactory;
import nablarch.common.dao.EntityList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.OptimisticLockException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThrows;

/**
 * {@link SqlTimeMetricsDaoContextFactory}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class SqlTimeMetricsDaoContextFactoryTest {
    private final SqlTimeMetricsDaoContextFactory sut = new SqlTimeMetricsDaoContextFactory();
    private final MockDaoContext originalDaoContext = new MockDaoContext();
    private final MeterRegistry meterRegistry = new SimpleMeterRegistry();

    @Before
    public void setUp() throws Exception {
        sut.setDelegate(new MockDaoContextFactory(originalDaoContext));
        sut.setMeterRegistry(meterRegistry);
    }

    @Test
    public void testCreate() {
        SqlTimeMetricsDaoContext daoContext = (SqlTimeMetricsDaoContext)sut.create();

        assertThat(daoContext.getDelegate(), is(sameInstance(originalDaoContext)));
        assertThat(daoContext.getMeterRegistry(), is(sameInstance(meterRegistry)));
    }

    @Test
    public void testThrowsExceptionIfDelegateIsNull() {
        sut.setDelegate(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, sut::create);

        assertThat(exception.getMessage(), is("delegate is null."));
    }

    @Test
    public void testThrowsExceptionIfMeterRegistryIsNull() {
        sut.setMeterRegistry(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, sut::create);

        assertThat(exception.getMessage(), is("meterRegistry is null."));
    }

    @Test
    public void testSetMetricsName() {
        sut.setMetricsName("test.metrics");

        SqlTimeMetricsDaoContext daoContext = (SqlTimeMetricsDaoContext) sut.create();

        assertThat(daoContext.getMetricsName(), is("test.metrics"));
    }

    @Test
    public void testSetMetricsDescription() {
        sut.setMetricsDescription("Test description");

        SqlTimeMetricsDaoContext daoContext = (SqlTimeMetricsDaoContext) sut.create();

        assertThat(daoContext.getMetricsDescription(), is("Test description"));
    }

    private static class MockDaoContextFactory extends DaoContextFactory {
        private final DaoContext daoContext;

        private MockDaoContextFactory(DaoContext daoContext) {
            this.daoContext = daoContext;
        }

        @Override
        public DaoContext create() {
            return daoContext;
        }
    }

    private static class MockDaoContext implements DaoContext {

        @Override
        public <T> T findById(Class<T> entityClass, Object... id) {
            return null;
        }

        @Override
        public <T> T findByIdOrNull(Class<T> aClass, Object... objects) {
            return null;
        }

        @Override
        public <T> EntityList<T> findAll(Class<T> entityClass) {
            return null;
        }

        @Override
        public <T> EntityList<T> findAllBySqlFile(Class<T> entityClass, String sqlId, Object params) {
            return null;
        }

        @Override
        public <T> EntityList<T> findAllBySqlFile(Class<T> entityClass, String sqlId) {
            return null;
        }

        @Override
        public <T> T findBySqlFile(Class<T> entityClass, String sqlId, Object params) {
            return null;
        }

        @Override
        public <T> T findBySqlFileOrNull(Class<T> aClass, String s, Object o) {
            return null;
        }

        @Override
        public <T> long countBySqlFile(Class<T> entityClass, String sqlId, Object params) {
            return 0;
        }

        @Override
        public <T> int update(T entity) throws OptimisticLockException {
            return 0;
        }

        @Override
        public <T> void batchUpdate(List<T> entities) {

        }

        @Override
        public <T> void insert(T entity) {

        }

        @Override
        public <T> void batchInsert(List<T> entities) {

        }

        @Override
        public <T> int delete(T entity) {
            return 0;
        }

        @Override
        public <T> void batchDelete(List<T> entities) {

        }

        @Override
        public DaoContext page(long page) {
            return null;
        }

        @Override
        public DaoContext per(long per) {
            return null;
        }

        @Override
        public DaoContext defer() {
            return null;
        }
    }
}