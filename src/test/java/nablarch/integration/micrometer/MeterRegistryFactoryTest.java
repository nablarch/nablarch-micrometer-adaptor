package nablarch.integration.micrometer;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import nablarch.core.repository.di.ComponentDefinitionLoader;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * {@link MeterRegistryFactory}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class MeterRegistryFactoryTest {

    @Test
    public void testLoadConfigurationAtClasspathRootIfXmlConfigPathIsNotSet() {
        MockMeterRegistryFactory sut = new MockMeterRegistryFactory();
        sut.createObject();

        Object value = sut.micrometerConfiguration.getComponentByName("foo.bar");
        assertThat(value, is("FOO_BAR"));
    }

    @Test
    public void testLoadConfigurationSpecifiedByXmlConfigPath() {
        MockMeterRegistryFactory sut = new MockMeterRegistryFactory();
        sut.setXmlConfigPath("nablarch/integration/micrometer/MeterRegistryFactoryTest/testLoadConfigurationSpecifiedByXmlConfigPath/test.xml");
        sut.createObject();

        Object value = sut.micrometerConfiguration.getComponentByName("test.value");
        assertThat(value, is("TESTVALUE"));
    }

    @Test
    public void testDefaultMeterBinderListProvider() {
        MockMeterRegistryFactory sut = new MockMeterRegistryFactory();
        assertThat(sut.meterBinderListProvider, instanceOf(DefaultMeterBinderListProvider.class));
    }

    @Test
    public void testAllMeterBindersBindToCreatedRegistry() {
        MockMeterRegistryFactory sut = new MockMeterRegistryFactory();

        MockMeterBinderListProvider meterBinderListProvider = new MockMeterBinderListProvider();
        sut.setMeterBinderListProvider(meterBinderListProvider);

        SimpleMeterRegistry createdRegistry = sut.createObject();

        assertThat(meterBinderListProvider.boundMeterRegistries, contains(createdRegistry, createdRegistry, createdRegistry));
    }

    @Test
    public void testCommonTags() {
        MockMeterRegistryFactory sut = new MockMeterRegistryFactory();
        Map<String, String> tags = new HashMap<>();
        tags.put("hello", "HELLO");
        tags.put("world", "WORLD");
        sut.setTags(tags);

        SimpleMeterRegistry registry = sut.createObject();
        for (Meter meter : registry.getMeters()) {
            assertThat(meter.getId() + ":hello", meter.getId().getTag("hello"), is("HELLO"));
            assertThat(meter.getId() + ":world", meter.getId().getTag("world"), is("WORLD"));
        }
    }

    @Test
    public void testCreatedComponentCanBeResolvedByType() {
        ComponentDefinitionLoader loader = new XmlComponentDefinitionLoader("nablarch/integration/micrometer/MeterRegistryFactoryTest/testCreatedComponentCanBeResolvedByType/components.xml");
        DiContainer container = new DiContainer(loader);

        assertThat(container.getComponentByType(FooRegistry.class), notNullValue());
        assertThat(container.getComponentByType(BarRegistry.class), notNullValue());
    }

    private static class MockMeterRegistryFactory extends MeterRegistryFactory<SimpleMeterRegistry> {
        private MicrometerConfiguration micrometerConfiguration;
        private SimpleMeterRegistry registry = new SimpleMeterRegistry();

        @Override
        public SimpleMeterRegistry createObject() {
            return doCreateObject();
        }

        @Override
        protected SimpleMeterRegistry createMeterRegistry(MicrometerConfiguration micrometerConfiguration) {
            this.micrometerConfiguration = micrometerConfiguration;
            return registry;
        }
    }

    private static class MockMeterBinderListProvider implements MeterBinderListProvider {
        private List<MeterRegistry> boundMeterRegistries = new ArrayList<>();

        @Override
        public List<MeterBinder> provide() {
            return Arrays.asList(
                boundMeterRegistries::add,
                boundMeterRegistries::add,
                boundMeterRegistries::add
            );
        }
    }

    public static class FooRegistry extends SimpleMeterRegistry {}
    public static class BarRegistry extends SimpleMeterRegistry {}

    public static class FooRegistryFactory extends MeterRegistryFactory<FooRegistry> {

        @Override
        public FooRegistry createObject() {
            return doCreateObject();
        }

        @Override
        protected FooRegistry createMeterRegistry(MicrometerConfiguration micrometerConfiguration) {
            return new FooRegistry();
        }
    }

    public static class BarRegistryFactory extends MeterRegistryFactory<BarRegistry> {

        @Override
        public BarRegistry createObject() {
            return doCreateObject();
        }

        @Override
        protected BarRegistry createMeterRegistry(MicrometerConfiguration micrometerConfiguration) {
            return new BarRegistry();
        }
    }
}