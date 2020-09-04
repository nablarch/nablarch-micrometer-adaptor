package nablarch.integration.micrometer;

import nablarch.core.repository.di.config.externalize.CompositeExternalizedLoader;
import nablarch.core.repository.di.config.externalize.OsEnvironmentVariableExternalizedLoader;
import nablarch.core.repository.di.config.externalize.SystemPropertyExternalizedLoader;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;

/**
 * {@link MicrometerConfiguration}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class MicrometerConfigurationTest {

    @Test
    public void testDefaultConstructorLoads_micrometer_xml_AtClasspathRoot() {
        MicrometerConfiguration sut = new MicrometerConfiguration();
        Object value = sut.getComponentByName("foo.bar");
        assertThat(value, is("FOO_BAR"));
    }

    @Test
    public void testLoadsXmlConfigSpecifiedByConstructor() {
        MicrometerConfiguration sut = new MicrometerConfiguration("nablarch/integration/micrometer/MicrometerConfigurationTest/testLoadsXmlConfigSpecifiedByConstructor/test.xml");
        Object value = sut.getComponentByName("fizz.buzz");
        assertThat(value, is("FIZZ_BUZZ"));
    }

    @Test
    public void testExternalizedLoadersAreOsEnvironmentLoaderAndSystemPropertyLoader() {
        MicrometerConfiguration sut = new MicrometerConfiguration();

        CompositeExternalizedLoader loader = (CompositeExternalizedLoader) sut.loadExternalizedComponentDefinitionLoader();
        assertThat(loader.getLoaders(), contains(
            instanceOf(OsEnvironmentVariableExternalizedLoader.class),
            instanceOf(SystemPropertyExternalizedLoader.class)
        ));
    }
}