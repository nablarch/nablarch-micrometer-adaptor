package nablarch.integration.micrometer.instrument.dao;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import nablarch.common.dao.DaoContext;
import nablarch.common.dao.EntityList;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SqlTimeMetricsDaoContext}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class SqlTimeMetricsDaoContextTest {
    private static final MockEntity MOCK_ENTITY = new MockEntity();
    private static final List<MockEntity> MOCK_ENTITY_LIST = Arrays.asList(new MockEntity(), new MockEntity(), new MockEntity());
    private static final List<?> EMPTY_ENTITY_LIST = Collections.emptyList();
    private static final Object PARAM = new Object();
    private static final Object[] PARAMS = new Object[] {new Object(), new Object(), new Object()};

    private final DaoContext daoContext = mock(DaoContext.class);

    private SqlTimeMetricsDaoContext sut;
    private SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry(SimpleConfig.DEFAULT, new Clock() {
        // 奇数番が計測開始時刻、偶数番が計測終了時刻を表す
        Iterator<Long> monotonicTimes = Arrays.asList(
            1000L, 2000L
        ).iterator();

        @Override
        public long monotonicTime() {
            return monotonicTimes.next();
        }

        @Override
        public long wallTime() { return 0; }
    });

    @Before
    public void setUp() {
        sut = new SqlTimeMetricsDaoContext(daoContext, meterRegistry);
    }

    @Test
    public void testBatchDelete() {
        sut.batchDelete(MOCK_ENTITY_LIST);

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();
        assertTimerRecord(timer, SqlTimeMetricsDaoContext.TAG_VALUE_NO_SQL_ID, "batchDelete");

        verify(daoContext).batchDelete(MOCK_ENTITY_LIST);
    }

    @Test
    public void testBatchDeleteWhenEntityListIsNull() {
        sut.batchDelete(null);

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();

        assertThat(timer, is(nullValue()));
        
        verify(daoContext).batchDelete(null);
    }

    @Test
    public void testBatchDeleteWhenEntityListIsEmpty() {
        sut.batchDelete(EMPTY_ENTITY_LIST);

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();

        assertThat(timer, is(nullValue()));

        verify(daoContext).batchDelete(EMPTY_ENTITY_LIST);
    }

    @Test
    public void testBatchDeleteCustomMetricsNameAndDescription() {
        sut.setMetricsName("test.metrics");
        sut.setMetricsDescription("Test metrics.");

        sut.batchDelete(MOCK_ENTITY_LIST);

        Timer timer = meterRegistry.find("test.metrics").timer();

        assertThat(timer.getId().getDescription(), is("Test metrics."));
    }

    @Test
    public void testBatchInsert() {
        sut.batchInsert(MOCK_ENTITY_LIST);

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();
        assertTimerRecord(timer, SqlTimeMetricsDaoContext.TAG_VALUE_NO_SQL_ID, "batchInsert");

        verify(daoContext).batchInsert(MOCK_ENTITY_LIST);
    }

    @Test
    public void testBatchInsertWhenEntityListIsNull() {
        sut.batchInsert(null);

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();

        assertThat(timer, is(nullValue()));

        verify(daoContext).batchInsert(null);
    }

    @Test
    public void testBatchInsertWhenEntityListIsEmpty() {
        sut.batchInsert(EMPTY_ENTITY_LIST);

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();

        assertThat(timer, is(nullValue()));

        verify(daoContext).batchInsert(EMPTY_ENTITY_LIST);
    }

    @Test
    public void testBatchInsertCustomMetricsNameAndDescription() {
        sut.setMetricsName("test.metrics");
        sut.setMetricsDescription("Test metrics.");

        sut.batchInsert(MOCK_ENTITY_LIST);

        Timer timer = meterRegistry.find("test.metrics").timer();

        assertThat(timer.getId().getDescription(), is("Test metrics."));
    }

    @Test
    public void testBatchUpdate() {
        sut.batchUpdate(MOCK_ENTITY_LIST);

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();
        assertTimerRecord(timer, SqlTimeMetricsDaoContext.TAG_VALUE_NO_SQL_ID, "batchUpdate");

        verify(daoContext).batchUpdate(MOCK_ENTITY_LIST);
    }

    @Test
    public void testBatchUpdateWhenEntityListIsNull() {
        sut.batchUpdate(null);

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();

        assertThat(timer, is(nullValue()));
        
        verify(daoContext).batchUpdate(null);
    }

    @Test
    public void testBatchUpdateWhenEntityListIsEmpty() {
        sut.batchUpdate(EMPTY_ENTITY_LIST);

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();

        assertThat(timer, is(nullValue()));
        
        verify(daoContext).batchUpdate(EMPTY_ENTITY_LIST);
    }

    @Test
    public void testBatchUpdateCustomMetricsNameAndDescription() {
        sut.setMetricsName("test.metrics");
        sut.setMetricsDescription("Test metrics.");

        sut.batchUpdate(MOCK_ENTITY_LIST);

        Timer timer = meterRegistry.find("test.metrics").timer();

        assertThat(timer.getId().getDescription(), is("Test metrics."));
    }

    @Test
    public void testCountBySqlFile() {
        when(daoContext.countBySqlFile(MockEntity.class, "test-sql-id", PARAM)).thenReturn(1234L);

        long returnValue = sut.countBySqlFile(MockEntity.class, "test-sql-id", PARAM);

        assertThat(returnValue, is(1234L));

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();
        assertTimerRecord(timer, "test-sql-id", "countBySqlFile");
    }

    @Test
    public void testCountBySqlFileCustomMetricsNameAndDescription() {
        sut.setMetricsName("test.metrics");
        sut.setMetricsDescription("Test metrics.");

        sut.countBySqlFile(MockEntity.class, "test-sql-id", PARAM);

        Timer timer = meterRegistry.find("test.metrics").timer();

        assertThat(timer.getId().getDescription(), is("Test metrics."));
    }

    @Test
    public void testDelete() {
        when(daoContext.delete(MOCK_ENTITY)).thenReturn(321);

        int returnValue = sut.delete(MOCK_ENTITY);

        assertThat(returnValue, is(321));

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();
        assertTimerRecord(timer, SqlTimeMetricsDaoContext.TAG_VALUE_NO_SQL_ID, "delete");
    }

    @Test
    public void testDeleteWhenEntityIsNull() {
        when(daoContext.delete(null)).thenReturn(123);

        int returnValue = sut.delete(null);

        assertThat(returnValue, is(123));

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();
        assertThat(timer, is(nullValue()));
    }

    @Test
    public void testDeleteCustomMetricsNameAndDescription() {
        sut.setMetricsName("test.metrics");
        sut.setMetricsDescription("Test metrics.");

        sut.delete(MOCK_ENTITY);

        Timer timer = meterRegistry.find("test.metrics").timer();

        assertThat(timer.getId().getDescription(), is("Test metrics."));
    }

    @Test
    public void testFindAll() {
        EntityList<MockEntity> entityList = new EntityList<>(Arrays.asList(new MockEntity(), new MockEntity()));
        when(daoContext.findAll(MockEntity.class)).thenReturn(entityList);

        EntityList<MockEntity> returnValue = sut.findAll(MockEntity.class);

        assertThat(returnValue, is(sameInstance(entityList)));

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();
        assertTimerRecord(timer, SqlTimeMetricsDaoContext.TAG_VALUE_NO_SQL_ID, "findAll");
    }

    @Test
    public void testFindAllCustomMetricsNameAndDescription() {
        sut.setMetricsName("test.metrics");
        sut.setMetricsDescription("Test metrics.");

        sut.findAll(MockEntity.class);

        Timer timer = meterRegistry.find("test.metrics").timer();

        assertThat(timer.getId().getDescription(), is("Test metrics."));
    }

    @Test
    public void testFindAllBySqlFile() {
        EntityList<MockEntity> entityList = new EntityList<>(Arrays.asList(new MockEntity(), new MockEntity()));
        when(daoContext.findAllBySqlFile(MockEntity.class, "test-sql-id", PARAM)).thenReturn(entityList);

        EntityList<MockEntity> returnValue = sut.findAllBySqlFile(MockEntity.class, "test-sql-id", PARAM);

        assertThat(returnValue, is(sameInstance(entityList)));

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();
        assertTimerRecord(timer, "test-sql-id", "findAllBySqlFile");
    }

    @Test
    public void testFindAllBySqlFileCustomMetricsNameAndDescription() {
        sut.setMetricsName("test.metrics");
        sut.setMetricsDescription("Test metrics.");

        sut.findAllBySqlFile(MockEntity.class, "test-sql-id", PARAM);

        Timer timer = meterRegistry.find("test.metrics").timer();

        assertThat(timer.getId().getDescription(), is("Test metrics."));
    }

    @Test
    public void testFindAllBySqlFileWithoutParams() {
        EntityList<MockEntity> entityList = new EntityList<>(Arrays.asList(new MockEntity(), new MockEntity()));
        when(daoContext.findAllBySqlFile(MockEntity.class, "test-sql-id")).thenReturn(entityList);

        EntityList<MockEntity> returnValue = sut.findAllBySqlFile(MockEntity.class, "test-sql-id");

        assertThat(returnValue, is(sameInstance(entityList)));

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();
        assertTimerRecord(timer, "test-sql-id", "findAllBySqlFile");
    }

    @Test
    public void testFindAllBySqlFileWithoutParamsCustomMetricsNameAndDescription() {
        sut.setMetricsName("test.metrics");
        sut.setMetricsDescription("Test metrics.");

        sut.findAllBySqlFile(MockEntity.class, "test-sql-id");

        Timer timer = meterRegistry.find("test.metrics").timer();

        assertThat(timer.getId().getDescription(), is("Test metrics."));
    }

    @Test
    public void testFindById() {
        when(daoContext.findById(MockEntity.class, PARAMS)).thenReturn(MOCK_ENTITY);

        MockEntity returnValue = sut.findById(MockEntity.class, PARAMS);

        assertThat(returnValue, is(sameInstance(MOCK_ENTITY)));

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();
        assertTimerRecord(timer, SqlTimeMetricsDaoContext.TAG_VALUE_NO_SQL_ID, "findById");
    }

    @Test
    public void testFindByIdOrNull() {
        when(daoContext.findByIdOrNull(MockEntity.class, PARAMS)).thenReturn(MOCK_ENTITY);

        MockEntity returnValue = sut.findByIdOrNull(MockEntity.class, PARAMS);

        assertThat(returnValue, is(sameInstance(MOCK_ENTITY)));

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();
        assertTimerRecord(timer, SqlTimeMetricsDaoContext.TAG_VALUE_NO_SQL_ID, "findByIdOrNull");
    }

    @Test
    public void testFindByIdCustomMetricsNameAndDescription() {
        sut.setMetricsName("test.metrics");
        sut.setMetricsDescription("Test metrics.");

        sut.findById(MockEntity.class, PARAMS);

        Timer timer = meterRegistry.find("test.metrics").timer();

        assertThat(timer.getId().getDescription(), is("Test metrics."));
    }

    @Test
    public void testFindByIdOrNullCustomMetricsNameAndDescription() {
        sut.setMetricsName("test.metrics");
        sut.setMetricsDescription("Test metrics.");

        sut.findByIdOrNull(MockEntity.class, PARAMS);

        Timer timer = meterRegistry.find("test.metrics").timer();

        assertThat(timer.getId().getDescription(), is("Test metrics."));
    }

    @Test
    public void testFindBySqlFile() {
        when(daoContext.findBySqlFile(MockEntity.class, "test-sql-id", PARAM)).thenReturn(MOCK_ENTITY);

        MockEntity returnValue = sut.findBySqlFile(MockEntity.class, "test-sql-id", PARAM);

        assertThat(returnValue, is(sameInstance(MOCK_ENTITY)));

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();
        assertTimerRecord(timer, "test-sql-id", "findBySqlFile");
    }

    @Test
    public void testFindBySqlFileOrNull() {
        when(daoContext.findBySqlFileOrNull(MockEntity.class, "test-sql-id", PARAM)).thenReturn(MOCK_ENTITY);

        MockEntity returnValue = sut.findBySqlFileOrNull(MockEntity.class, "test-sql-id", PARAM);

        assertThat(returnValue, is(sameInstance(MOCK_ENTITY)));

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();
        assertTimerRecord(timer, "test-sql-id", "findBySqlFileOrNull");
    }

    @Test
    public void testFindBySqlFileCustomMetricsNameAndDescription() {
        sut.setMetricsName("test.metrics");
        sut.setMetricsDescription("Test metrics.");

        sut.findBySqlFile(MockEntity.class,"test-sql-id",PARAMS);

        Timer timer = meterRegistry.find("test.metrics").timer();

        assertThat(timer.getId().getDescription(), is("Test metrics."));
    }

    @Test
    public void testFindBySqlOrNullFileCustomMetricsNameAndDescription() {
        sut.setMetricsName("test.metrics");
        sut.setMetricsDescription("Test metrics.");

        sut.findBySqlFileOrNull(MockEntity.class,"test-sql-id",PARAMS);

        Timer timer = meterRegistry.find("test.metrics").timer();

        assertThat(timer.getId().getDescription(), is("Test metrics."));
    }

    @Test
    public void testUpdate() {
        when(daoContext.update(MOCK_ENTITY)).thenReturn(33);

        int returnValue = sut.update(MOCK_ENTITY);

        assertThat(returnValue, is(33));

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();
        assertTimerRecord(timer, SqlTimeMetricsDaoContext.TAG_VALUE_NO_SQL_ID, "update");
    }

    @Test
    public void testUpdateWhenEntityIsNull() {
        when(daoContext.update(null)).thenReturn(44);

        int returnValue = sut.update(null);

        assertThat(returnValue, is(44));

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();
        assertThat(timer, is(nullValue()));
    }

    @Test
    public void testUpdateCustomMetricsNameAndDescription() {
        sut.setMetricsName("test.metrics");
        sut.setMetricsDescription("Test metrics.");

        sut.update(MOCK_ENTITY);

        Timer timer = meterRegistry.find("test.metrics").timer();

        assertThat(timer.getId().getDescription(), is("Test metrics."));
    }

    @Test
    public void testInsert() {
        sut.insert(MOCK_ENTITY);

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();
        assertTimerRecord(timer, SqlTimeMetricsDaoContext.TAG_VALUE_NO_SQL_ID, "insert");

        verify(daoContext).insert(MOCK_ENTITY);
    }

    @Test
    public void testInsertWhenEntityIsNull() {
        sut.insert(null);

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();
        assertThat(timer, is(nullValue()));

        verify(daoContext).insert(null);
    }

    @Test
    public void testInsertCustomMetricsNameAndDescription() {
        sut.setMetricsName("test.metrics");
        sut.setMetricsDescription("Test metrics.");

        sut.insert(MOCK_ENTITY);

        Timer timer = meterRegistry.find("test.metrics").timer();

        assertThat(timer.getId().getDescription(), is("Test metrics."));
    }

    @Test
    public void testPage() {
        DaoContext returnValue = sut.page(40L);

        assertThat(returnValue, is(sameInstance(sut)));

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();
        assertThat(timer, is(nullValue()));

        verify(daoContext).page(40L);
    }

    @Test
    public void testPer() {
        DaoContext returnValue = sut.per(30L);

        assertThat(returnValue, is(sameInstance(sut)));

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();
        assertThat(timer, is(nullValue()));

        verify(daoContext).per(30L);
    }

    @Test
    public void testDefer() {
        DaoContext returnValue = sut.defer();

        assertThat(returnValue, is(sameInstance(sut)));

        Timer timer = meterRegistry.find(SqlTimeMetricsDaoContext.DEFAULT_METRICS_NAME).timer();
        assertThat(timer, is(nullValue()));

        verify(daoContext).defer();
    }

    @Test
    public void testGetMetricsName() {
        sut.setMetricsName("foo");

        String metricsName = sut.getMetricsName();

        assertThat(metricsName, is("foo"));
    }

    @Test
    public void testGetMetricsDescription() {
        sut.setMetricsDescription("bar");

        String metricsDescription = sut.getMetricsDescription();

        assertThat(metricsDescription, is("bar"));
    }

    private void assertTimerRecord(Timer timer, String expectedSqlId, String expectedMethodName) {
        Meter.Id id = timer.getId();

        assertThat("sql.id tag",
                id.getTag(SqlTimeMetricsDaoContext.TAG_NAME_SQL_ID), is(expectedSqlId));
        assertThat("entity tag",
                id.getTag(SqlTimeMetricsDaoContext.TAG_NAME_ENTITY_NAME), is(MockEntity.class.getName()));
        assertThat("method tag",
                id.getTag(SqlTimeMetricsDaoContext.TAG_NAME_METHOD_NAME), is(expectedMethodName));
        assertThat("metrics description",
                id.getDescription(), is(SqlTimeMetricsDaoContext.DEFAULT_METRICS_DESCRIPTION));

        assertThat("recorded time", timer.totalTime(TimeUnit.NANOSECONDS), is(1000.0));
    }

    @Test
    public void testGetDelegate() {
        assertThat(sut.getDelegate(), is(sameInstance(daoContext)));
    }

    public static class MockEntity {
    }
}