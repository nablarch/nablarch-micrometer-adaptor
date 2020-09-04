package nablarch.integration.micrometer.statsd;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * {@link NablarchStatsdConfig}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class NablarchStatsdConfigTest {

    @Test
    public void testSubPrefix() {
        NablarchStatsdConfig sut = new NablarchStatsdConfig(null, null);
        assertThat(sut.subPrefix(), is("statsd"));
    }
}