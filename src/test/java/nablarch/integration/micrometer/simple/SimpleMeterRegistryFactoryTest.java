package nablarch.integration.micrometer.simple;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import nablarch.core.repository.disposal.BasicApplicationDisposer;
import nablarch.integration.micrometer.DefaultMeterBinderListProvider;
import nablarch.test.support.reflection.ReflectionUtil;
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
        sut.setApplicationDisposer(new BasicApplicationDisposer());
        sut.setMeterBinderListProvider(new DefaultMeterBinderListProvider());
        sut.setPrefix("test.simple");
        sut.setXmlConfigPath("nablarch/integration/micrometer/simple/SimpleMeterRegistryFactory/testCreateObject/test.xml");

        SimpleMeterRegistry meterRegistry = sut.createObject();

        NablarchSimpleConfig config = ReflectionUtil.getFieldValue(meterRegistry, "config");
        assertThat(config.step(), is(Duration.ofSeconds(3456)));
    }
}