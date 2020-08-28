package nablarch.integration.micrometer.cloudwatch;

import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.Verifications;
import nablarch.integration.micrometer.MicrometerConfiguration;
import org.junit.Before;
import org.junit.Test;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

/**
 * {@link CloudWatchMeterRegistryFactory}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class CloudWatchMeterRegistryFactoryTest {
    @Mocked
    private CloudWatchAsyncClient cloudWatchAsyncClient;

    private CloudWatchMeterRegistryFactory sut = new CloudWatchMeterRegistryFactory();
    private MicrometerConfiguration micrometerConfig = new MicrometerConfiguration("nablarch/integration/micrometer/cloudwatch/CloudWatchMeterRegistryFactoryTest/test.xml");

    @Before
    public void setup() {
        sut.setPrefix("test.cloudwatch");
    }

    @Test
    public void testCreateMeterRegistry() {
        CloudWatchMeterRegistry meterRegistry = sut.createMeterRegistry(micrometerConfig);

        NablarchCloudWatchConfig config = Deencapsulation.getField(meterRegistry, "config");
        assertThat(config.namespace(), is("cloudwatch-test-namespace"));

        new Verifications() {{
            CloudWatchAsyncClient.create(); times = 1;
        }};
    }

    @Test
    public void testCloudWatchAsyncClientProvider() {
        sut.setCloudWatchAsyncClientProvider(() -> cloudWatchAsyncClient);

        CloudWatchMeterRegistry meterRegistry = sut.createMeterRegistry(micrometerConfig);

        CloudWatchAsyncClient cloudWatchAsyncClient = Deencapsulation.getField(meterRegistry, "cloudWatchAsyncClient");
        assertThat(cloudWatchAsyncClient, is(sameInstance(this.cloudWatchAsyncClient)));

        new Verifications() {{
            CloudWatchAsyncClient.create(); times = 0;
        }};
    }
}