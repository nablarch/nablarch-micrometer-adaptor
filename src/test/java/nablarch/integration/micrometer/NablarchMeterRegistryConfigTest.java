package nablarch.integration.micrometer;

import nablarch.core.repository.di.DiContainer;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link NablarchMeterRegistryConfig}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class NablarchMeterRegistryConfigTest {
    private final DiContainer diContainer = mock(DiContainer.class);

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
        when(diContainer.getComponentByName("foo.bar")).thenReturn("FOO_BAR");

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