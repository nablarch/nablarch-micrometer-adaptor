package nablarch.integration.micrometer.statsd;

import io.micrometer.statsd.StatsdMeterRegistry;
import nablarch.core.repository.disposal.BasicApplicationDisposer;
import nablarch.integration.micrometer.DefaultMeterBinderListProvider;
import nablarch.test.support.reflection.ReflectionUtil;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * {@link StatsdMeterRegistryFactory}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class StatsdMeterRegistryFactoryTest {

    @Test
    public void testCreateObject() {
        StatsdMeterRegistryFactory sut = new StatsdMeterRegistryFactory();
        sut.setApplicationDisposer(new BasicApplicationDisposer());
        sut.setMeterBinderListProvider(new DefaultMeterBinderListProvider());
        sut.setPrefix("test.statsd");
        sut.setXmlConfigPath("nablarch/integration/micrometer/statsd/StatsdMeterRegistryFactory/testCreateObject/test.xml");

        StatsdMeterRegistry meterRegistry = sut.createObject();

        NablarchStatsdConfig config = ReflectionUtil.getFieldValue(meterRegistry, "statsdConfig");
        assertThat(config.host(), is("test-statsd-host"));
    }
}