package nablarch.integration.micrometer.otlp;

import io.micrometer.registry.otlp.OtlpMeterRegistry;
import mockit.Deencapsulation;
import nablarch.core.repository.disposal.BasicApplicationDisposer;
import nablarch.integration.micrometer.DefaultMeterBinderListProvider;
import org.junit.Test;

import java.time.Duration;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * {@link OtlpMeterRegistryFactory}の単体テスト。
 * @author Junya Koyama
 */
public class OtlpMeterRegistryFactoryTest {

    /**
     * 空ではないpropertiesファイルを設定した時のテストケース。
     * @author Junya Koyama
     */
    @Test
    public void testCreateObject() {
        OtlpMeterRegistryFactory sut = new OtlpMeterRegistryFactory();
        sut.setApplicationDisposer(new BasicApplicationDisposer());
        sut.setMeterBinderListProvider(new DefaultMeterBinderListProvider());
        sut.setPrefix("test.otlp");
        sut.setXmlConfigPath("nablarch/integration/micrometer/otlp/OtlpMeterRegistryFactory/testCreateObject/test.xml");

        OtlpMeterRegistry meterRegistry = sut.createObject();

        NablarchOtlpConfig config = Deencapsulation.getField(meterRegistry, "config");

        // setPrefixを行うと、subPrefixとは関係なく指定のprefixを取得するようになる
        assertThat(config.prefix(), is("test.otlp"));
        // From OtlpConfig
        assertThat(config.url(), is("http://localhost:9090/api/v1/otlp/v1/metrics"));
        assertThat(config.resourceAttributes(), allOf(
                hasEntry("service.name", "nablarch-test"),
                hasEntry("url.scheme", "http"),
                hasEntry("service.version", "v1alpha1")
        ));

        assertThat(config.validate().failures(), empty());
        assertThat(config.validate().isValid(), is(true));
    }

    /**
     * 空のpropertiesファイルを設定し、環境変数も設定しない時のテストケース。
     * @author Junya Koyama
     */
    @Test
    public void testDefault() {
        // NablarchOtlpConfigは継承関係が複雑なので、デフォルト値の挙動をテストで示しておく。
        OtlpMeterRegistryFactory sut = new OtlpMeterRegistryFactory();
        sut.setApplicationDisposer(new BasicApplicationDisposer());
        sut.setMeterBinderListProvider(new DefaultMeterBinderListProvider());
        sut.setXmlConfigPath("nablarch/integration/micrometer/otlp/OtlpMeterRegistryFactory/testCreateObject/test.empty.xml");

        OtlpMeterRegistry meterRegistry = sut.createObject();
        NablarchOtlpConfig config = Deencapsulation.getField(meterRegistry, "config");

        // From NablarchOtlpConfig
        assertThat(config.subPrefix(), is("otlp"));
        // From NablarchMeterRegistryConfig
        assertThat(config.prefix(), allOf(
                is(String.join(".", "nablarch.micrometer", config.subPrefix())),
                is("nablarch.micrometer.otlp")
        ));
        // From OtlpConfig
        assertThat(config.url(), is("http://localhost:4318/v1/metrics"));
        assertThat(config.resourceAttributes(), is(Collections.<String, String>emptyMap()));
        // From PushRegistryConfig
        assertThat(config.step(), is(Duration.ofMinutes(1L)));
        assertThat(config.enabled(), is(true));
        assertThat(config.batchSize(), is(10000));

        assertThat(config.validate().failures(), empty());
        assertThat(config.validate().isValid(), is(true));
    }
}
