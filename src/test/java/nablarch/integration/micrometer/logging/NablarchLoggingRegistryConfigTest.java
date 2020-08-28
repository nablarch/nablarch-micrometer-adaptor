package nablarch.integration.micrometer.logging;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * {@link NablarchLoggingRegistryConfig}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class NablarchLoggingRegistryConfigTest {

    @Test
    public void testSubPrefix() {
        NablarchLoggingRegistryConfig sut = new NablarchLoggingRegistryConfig(null, null);
        assertThat(sut.subPrefix(), is("logging"));
    }
}