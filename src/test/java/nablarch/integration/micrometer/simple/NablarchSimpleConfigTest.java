package nablarch.integration.micrometer.simple;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * {@link NablarchSimpleConfig}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class NablarchSimpleConfigTest {

    @Test
    public void testSubPrefix() {
        NablarchSimpleConfig sut = new NablarchSimpleConfig(null, null);
        assertThat(sut.subPrefix(), is("simple"));
    }
}