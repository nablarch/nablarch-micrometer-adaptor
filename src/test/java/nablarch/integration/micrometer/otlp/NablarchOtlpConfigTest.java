package nablarch.integration.micrometer.otlp;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * {@link NablarchOtlpConfig}の単体テスト。
 * @author Junya Koyama
 */
public class NablarchOtlpConfigTest {

    @Test
    public void testSubPrefix() {
        NablarchOtlpConfig sut = new NablarchOtlpConfig(null, null);
        assertThat(sut.subPrefix(), is("otlp"));
    }
}
