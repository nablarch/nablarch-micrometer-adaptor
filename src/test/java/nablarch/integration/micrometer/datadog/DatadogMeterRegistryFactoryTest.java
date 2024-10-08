package nablarch.integration.micrometer.datadog;

import io.micrometer.datadog.DatadogMeterRegistry;
import nablarch.core.repository.disposal.BasicApplicationDisposer;
import nablarch.integration.micrometer.DefaultMeterBinderListProvider;
import nablarch.test.support.reflection.ReflectionUtil;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * {@link DatadogMeterRegistryFactory}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class DatadogMeterRegistryFactoryTest {

    @Test
    public void testCreateObject() {
        DatadogMeterRegistryFactory sut = new DatadogMeterRegistryFactory();
        sut.setApplicationDisposer(new BasicApplicationDisposer());
        sut.setMeterBinderListProvider(new DefaultMeterBinderListProvider());
        sut.setPrefix("test.datadog");
        sut.setXmlConfigPath("nablarch/integration/micrometer/datadog/DatadogMeterRegistryFactoryTest/testCreateObject/test.xml");

        DatadogMeterRegistry meterRegistry = sut.createObject();

        NablarchDatadogConfig config = ReflectionUtil.getFieldValue(meterRegistry, "config");
        assertThat(config.apiKey(), is("datadog-test-api-key"));
    }
}