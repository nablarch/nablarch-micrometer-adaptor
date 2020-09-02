package nablarch.integration.micrometer.simple;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import mockit.Deencapsulation;
import nablarch.integration.micrometer.MicrometerConfiguration;
import org.junit.Test;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * {@link SimpleMeterRegistryFactory}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class SimpleMeterRegistryFactoryTest {

    @Test
    public void testCreateObject() {
        SimpleMeterRegistryFactory sut = new SimpleMeterRegistryFactory();
        sut.setPrefix("test.simple");
        sut.setXmlConfigPath("nablarch/integration/micrometer/simple/SimpleMeterRegistryFactory/testCreateObject/test.xml");

        SimpleMeterRegistry meterRegistry = sut.createObject();

        NablarchSimpleConfig config = Deencapsulation.getField(meterRegistry, "config");
        assertThat(config.step(), is(Duration.ofSeconds(3456)));
    }
}