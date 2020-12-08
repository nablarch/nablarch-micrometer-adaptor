package nablarch.integration.micrometer.instrument.binder.jmx;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import mockit.Expectations;
import mockit.Mocked;
import nablarch.integration.micrometer.MockJulHandler;
import nablarch.integration.micrometer.instrument.binder.MetricsMetaData;
import org.junit.Before;
import org.junit.Test;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * {@link JmxGaugeMetrics}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class JmxGaugeMetricsTest {
    @Mocked
    private ManagementFactory managementFactory;
    @Mocked
    private MBeanServer mBeanServer;

    private ObjectName objectName;
    private String attributeName;
    private MBeanAttributeCondition mBeanAttributeCondition;
    private MetricsMetaData metricsMetaData;
    private SimpleMeterRegistry meterRegistry;

    private JmxGaugeMetrics sut;

    private MockJulHandler mockJulHandler;

    @Before
    public void setUp() throws Exception {
        Logger logger = Logger.getLogger("");
        mockJulHandler = new MockJulHandler();
        logger.addHandler(mockJulHandler);

        new Expectations() {{
            ManagementFactory.getPlatformMBeanServer(); result = mBeanServer;
        }};

        attributeName = "ATTRIBUTE";
        objectName = new ObjectName("test:type=Foo");
        mBeanAttributeCondition =
                new MBeanAttributeCondition(objectName.getCanonicalName(), attributeName);

        metricsMetaData = new MetricsMetaData(
                "metrics.name", "Metrics Description", Arrays.asList(Tag.of("foo", "FOO"), Tag.of("bar", "BAR")));

        sut = new JmxGaugeMetrics(metricsMetaData, mBeanAttributeCondition);

        meterRegistry = new SimpleMeterRegistry();
        sut.bindTo(meterRegistry);
    }

    @Test
    public void testMeasureGaugeValueFromSpecifiedMBeanAttribute() throws Exception {
        new Expectations() {{
            mBeanServer.getAttribute(objectName, attributeName); result = 12.34;
        }};

        Gauge gauge = meterRegistry.find(metricsMetaData.getName()).gauge();

        Meter.Id id = gauge.getId();

        assertThat(id.getDescription(), is(metricsMetaData.getDescription()));
        assertThat(id.getTags(), containsInAnyOrder(toArray(metricsMetaData.getTags())));
        assertThat(gauge.value(), is(12.34));
    }

    @Test
    public void testMeasureGaugeValueAsNaNIfAttributeValueIsNotNumber() throws Exception {
        new Expectations() {{
            mBeanServer.getAttribute(objectName, attributeName); result = "NotNumber";
        }};

        Gauge gauge = meterRegistry.find(metricsMetaData.getName()).gauge();

        assertThat(gauge.value(), is(Double.NaN));
    }

    @Test
    public void testWarnLogIfJMExceptionIsThrown() throws Exception {
        // Gaugeの値を取得するときに例外がスローされると、Micrometerは内部のロギング(InternalLogger)を使って警告ログを出力する。
        // InternalLoggerの実体は、クラスパスにSLF4Jの実装が含まれていればそれを使用し、なければJULを使用する(InternalLoggerFactoryを参照)。
        // ここでは、JULが使用されている前提で、例外発生時にJULに警告ログが出力されていること(例外を再スローできていること)をテストしている。
        JMException jmException = new JMException("test");

        new Expectations() {{
            mBeanServer.getAttribute(objectName, attributeName); result = jmException;
        }};

        Gauge gauge = meterRegistry.find(metricsMetaData.getName()).gauge();

        assertThat(gauge.value(), is(Double.NaN));

        assertThat(mockJulHandler.getPublishedRecordList(), is(hasSize(1)));

        LogRecord logRecord = mockJulHandler.getPublishedRecordList().get(0);
        assertThat(logRecord.getLevel(), is(Level.WARNING));
        assertThat(logRecord.getThrown().getCause(), is(jmException));
    }

    private Object[] toArray(Iterable<Tag> tags) {
        List<Tag> list = new ArrayList<>();
        tags.iterator().forEachRemaining(list::add);
        return list.toArray();
    }
}