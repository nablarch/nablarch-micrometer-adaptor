package nablarch.integration.micrometer.otlp;

import io.micrometer.core.instrument.config.validate.Validated;
import io.micrometer.core.instrument.config.validate.ValidationException;
import io.micrometer.registry.otlp.AggregationTemporality;
import io.micrometer.registry.otlp.OtlpMeterRegistry;
import mockit.Deencapsulation;
import nablarch.core.repository.disposal.BasicApplicationDisposer;
import nablarch.integration.micrometer.DefaultMeterBinderListProvider;
import org.junit.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThrows;

/**
 * {@link OtlpMeterRegistryFactory}の単体テスト。
 * @author Junya Koyama
 */
public class OtlpMeterRegistryFactoryTest {

    /**
     * 空ではないpropertiesファイルを設定した時のテストケース。
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
        assertThat(config.aggregationTemporality(), is(AggregationTemporality.DELTA));
        assertThat(config.baseTimeUnit(), is(TimeUnit.SECONDS));
        assertThat(config.headers(), hasEntry("Api-Key", "NRIA****"));
        assertThat(config.resourceAttributes(), allOf(
                hasEntry("service.name", "nablarch-test"),
                hasEntry("url.scheme", "http"),
                hasEntry("service.version", "v1alpha1")
        ));
        assertThat(config.url(), is("http://localhost:9090/api/v1/otlp/v1/metrics"));

        assertThat(config.validate().failures(), empty());
        assertThat(config.validate().isValid(), is(true));
    }

    /**
     * 空ではない、精査エラーとなるpropertiesファイルを設定した時のテストケース。
     */
    @Test
    public void testInvalid() {
        OtlpMeterRegistryFactory sut = new OtlpMeterRegistryFactory();
        sut.setApplicationDisposer(new BasicApplicationDisposer());
        sut.setMeterBinderListProvider(new DefaultMeterBinderListProvider());
        sut.setPrefix("test.otlp");
        sut.setXmlConfigPath("nablarch/integration/micrometer/otlp/OtlpMeterRegistryFactory/testCreateObject/test.invalid.xml");

        ValidationException thrown = assertThrows(ValidationException.class, sut::createObject);
        List<String> errors = thrown.getValidation().failures()
                .stream().map(Validated.Invalid::getMessage).collect(Collectors.toList());
        assertThat(errors, contains(
                "must contain a valid time unit",
                "should be one of 'DELTA', 'CUMULATIVE'"
        ));
    }

    /**
     * 空のpropertiesファイルを設定し、環境変数も設定しない時のテストケース。
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
        assertThat(config.prefix(), is("nablarch.micrometer.otlp"));
        // From OtlpConfig
        assertThat(config.aggregationTemporality(), is(AggregationTemporality.CUMULATIVE));
        assertThat(config.baseTimeUnit(), is(TimeUnit.MILLISECONDS));
        assertThat(config.headers(), is(Collections.<String, String>emptyMap()));
        assertThat(config.resourceAttributes(), is(Collections.<String, String>emptyMap()));
        assertThat(config.url(), is("http://localhost:4318/v1/metrics"));
        // From PushRegistryConfig
        assertThat(config.step(), is(Duration.ofMinutes(1L)));
        assertThat(config.enabled(), is(true));
        assertThat(config.batchSize(), is(10000));

        assertThat(config.validate().failures(), empty());
        assertThat(config.validate().isValid(), is(true));
    }
}
