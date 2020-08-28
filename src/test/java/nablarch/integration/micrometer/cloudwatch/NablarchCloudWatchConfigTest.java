package nablarch.integration.micrometer.cloudwatch;

import nablarch.integration.micrometer.datadog.DatadogMeterRegistryFactory;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * {@link NablarchCloudWatchConfig}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class NablarchCloudWatchConfigTest {

    @Test
    public void testSubPrefix() {
        NablarchCloudWatchConfig sut = new NablarchCloudWatchConfig(null, null);
        assertThat(sut.subPrefix(), is("cloudwatch"));
    }
}