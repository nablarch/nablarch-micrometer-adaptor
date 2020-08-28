package nablarch.integration.micrometer.datadog;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * {@link NablarchDatadogConfig}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class NablarchDatadogConfigTest {

    @Test
    public void testSubPrefix() {
        NablarchDatadogConfig sut = new NablarchDatadogConfig(null, null);
        assertThat(sut.subPrefix(), is("datadog"));
    }
}