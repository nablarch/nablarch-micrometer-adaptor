package nablarch.integration.micrometer.statsd;

import io.micrometer.statsd.StatsdMeterRegistry;
import mockit.Deencapsulation;
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
    public void testCreateMeterRegistry() {
        StatsdMeterRegistryFactory sut = new StatsdMeterRegistryFactory();
        sut.setPrefix("test.statsd");
        MicrometerConfiguration micrometerConfig = new MicrometerConfiguration("nablarch/integration/micrometer/statsd/StatsdMeterRegistryFactory/testCreateMeterRegistry/test.xml");

        StatsdMeterRegistry meterRegistry = sut.createMeterRegistry(micrometerConfig);

        NablarchStatsdConfig config = Deencapsulation.getField(meterRegistry, "statsdConfig");
        assertThat(config.host(), is("test-statsd-host"));
    }
}