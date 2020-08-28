package nablarch.integration.micrometer;

import mockit.Expectations;
import mockit.Mocked;
import nablarch.core.repository.di.DiContainer;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * {@link NablarchMeterRegistryConfig}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class NablarchMeterRegistryConfigTest {
    @Mocked
    private DiContainer diContainer;

    @Test
    public void testPrefixIsBuiltBySubPrefixIfPrefixIsNull() {
        NablarchMeterRegistryConfig sut = new NablarchMeterRegistryConfig(null, diContainer) {
            @Override
            protected String subPrefix() {
                return "subprefix";
            }
        };

        String prefix = sut.prefix();
        assertThat(prefix, is("nablarch.micrometer.subprefix"));
    }

    @Test
    public void testPrefixIsReturnedIfPrefixIsNotNull() {
        NablarchMeterRegistryConfig sut = new NablarchMeterRegistryConfig("custom.prefix", diContainer) {
            @Override
            protected String subPrefix() {
                return "subprefix";
            }
        };

        String prefix = sut.prefix();
        assertThat(prefix, is("custom.prefix"));
    }

    @Test
    public void testGetMethodDelegatesToDiContainer() {
        new Expectations() {{
            diContainer.getComponentByName("foo.bar"); result = "FOO_BAR";
        }};

        NablarchMeterRegistryConfig sut = new NablarchMeterRegistryConfig(null, diContainer) {
            @Override
            protected String subPrefix() {
                return null;
            }
        };

        String value = sut.get("foo.bar");
        assertThat(value, is("FOO_BAR"));
    }
}