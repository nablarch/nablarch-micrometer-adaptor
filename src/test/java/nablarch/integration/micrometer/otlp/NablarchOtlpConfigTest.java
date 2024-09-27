package nablarch.integration.micrometer.otlp;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;

/**
 * {@link NablarchOtlpConfig}の単体テスト。
 *
 * @author Junya Koyama
 */
public class NablarchOtlpConfigTest {

    @Test
    public void testSubPrefix() {
        NablarchOtlpConfig sut = new NablarchOtlpConfig(null, null);
        // From NablarchOtlpConfig
        assertThat(sut.subPrefix(), is("otlp"));
    }

    /**
     * {@link nablarch.integration.micrometer.NablarchMeterRegistryConfig}のデフォルト値テスト。
     */
    @Test
    public void testDefaultFromNablarchMeterRegistryConfig() {
        NablarchOtlpConfig sut = new NablarchOtlpConfig(null, null);
        assertThat(sut.prefix(), is("nablarch.micrometer.otlp"));
    }

    /**
     * {@link io.micrometer.registry.otlp.OtlpConfig}のデフォルト値テスト。
     */
    @Test
    public void testDefaultFromOtlpConfig() {
        // diContainerをnullにすると、NullPointerExceptionとなる。
        // 実環境でも同様に、propertiesファイルの内容が空であっても配置しなければNullPointerExceptionとなる。
        NablarchOtlpConfig sut = new NablarchOtlpConfig(null, null);
        assertThrows(NullPointerException.class, sut::url);
    }
}
