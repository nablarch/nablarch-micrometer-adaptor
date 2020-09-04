package nablarch.integration.micrometer.cloudwatch;

import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.Verifications;
import nablarch.core.repository.disposal.ApplicationDisposer;
import nablarch.core.repository.disposal.BasicApplicationDisposer;
import nablarch.integration.micrometer.DefaultMeterBinderListProvider;
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
    private ApplicationDisposer applicationDisposer = new BasicApplicationDisposer();

    @Before
    public void setup() {
        sut.setApplicationDisposer(applicationDisposer);
        sut.setMeterBinderListProvider(new DefaultMeterBinderListProvider());
        sut.setPrefix("test.cloudwatch");
        sut.setXmlConfigPath("nablarch/integration/micrometer/cloudwatch/CloudWatchMeterRegistryFactoryTest/test.xml");
    }

    @Test
    public void testCreateObject() {
        CloudWatchMeterRegistry meterRegistry = sut.createObject();

        NablarchCloudWatchConfig config = Deencapsulation.getField(meterRegistry, "config");
        assertThat(config.namespace(), is("cloudwatch-test-namespace"));

        new Verifications() {{
            CloudWatchAsyncClient.create(); times = 1;
        }};
    }

    @Test
    public void testCloudWatchAsyncClientProvider() {
        sut.setCloudWatchAsyncClientProvider(() -> cloudWatchAsyncClient);

        CloudWatchMeterRegistry meterRegistry = sut.createObject();

        CloudWatchAsyncClient cloudWatchAsyncClient = Deencapsulation.getField(meterRegistry, "cloudWatchAsyncClient");
        assertThat(cloudWatchAsyncClient, is(sameInstance(this.cloudWatchAsyncClient)));

        new Verifications() {{
            CloudWatchAsyncClient.create(); times = 0;
        }};
    }

    @Test
    public void testDisposeCloudWatchAsyncClient() {
        sut.createObject();
        applicationDisposer.dispose();

        new Verifications() {{
            cloudWatchAsyncClient.close(); times = 1;
        }};
    }
}