package nablarch.integration.micrometer.statsd;

import io.micrometer.statsd.StatsdMeterRegistry;
import mockit.Deencapsulation;
import nablarch.integration.micrometer.DefaultMeterBinderListProvider;
import nablarch.integration.micrometer.MicrometerConfiguration;
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
        sut.setMeterBinderListProvider(new DefaultMeterBinderListProvider());
        sut.setPrefix("test.statsd");
        sut.setXmlConfigPath("nablarch/integration/micrometer/statsd/StatsdMeterRegistryFactory/testCreateObject/test.xml");

        StatsdMeterRegistry meterRegistry = sut.createObject();

        NablarchStatsdConfig config = Deencapsulation.getField(meterRegistry, "statsdConfig");
        assertThat(config.host(), is("test-statsd-host"));
    }
}