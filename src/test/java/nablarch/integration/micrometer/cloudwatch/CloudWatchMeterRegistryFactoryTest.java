package nablarch.integration.micrometer.cloudwatch;

import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import nablarch.core.repository.disposal.ApplicationDisposer;
import nablarch.core.repository.disposal.BasicApplicationDisposer;
import nablarch.integration.micrometer.DefaultMeterBinderListProvider;
import nablarch.test.support.reflection.ReflectionUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * {@link CloudWatchMeterRegistryFactory}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class CloudWatchMeterRegistryFactoryTest {
    private final MockedStatic<CloudWatchAsyncClient> cloudWatchAsyncClientMockedStatic = mockStatic(CloudWatchAsyncClient.class, RETURNS_DEEP_STUBS);
    private final CloudWatchAsyncClient cloudWatchAsyncClient = mock(CloudWatchAsyncClient.class);

    private CloudWatchMeterRegistryFactory sut = new CloudWatchMeterRegistryFactory();
    private ApplicationDisposer applicationDisposer = new BasicApplicationDisposer();

    @Before
    public void setup() {
        cloudWatchAsyncClientMockedStatic.when(CloudWatchAsyncClient::create).thenReturn(cloudWatchAsyncClient);
        
        sut.setApplicationDisposer(applicationDisposer);
        sut.setMeterBinderListProvider(new DefaultMeterBinderListProvider());
        sut.setPrefix("test.cloudwatch");
        sut.setXmlConfigPath("nablarch/integration/micrometer/cloudwatch/CloudWatchMeterRegistryFactoryTest/test.xml");
    }

    @After
    public void tearDown() {
        cloudWatchAsyncClientMockedStatic.close();
    }

    @Test
    public void testCreateObject() {
        CloudWatchMeterRegistry meterRegistry = sut.createObject();

        NablarchCloudWatchConfig config = ReflectionUtil.getFieldValue(meterRegistry, "config");
        assertThat(config.namespace(), is("cloudwatch-test-namespace"));

        cloudWatchAsyncClientMockedStatic.verify(CloudWatchAsyncClient::create);
    }

    @Test
    public void testCloudWatchAsyncClientProvider() {
        sut.setCloudWatchAsyncClientProvider(() -> cloudWatchAsyncClient);

        CloudWatchMeterRegistry meterRegistry = sut.createObject();

        CloudWatchAsyncClient cloudWatchAsyncClient = ReflectionUtil.getFieldValue(meterRegistry, "cloudWatchAsyncClient");
        assertThat(cloudWatchAsyncClient, is(sameInstance(this.cloudWatchAsyncClient)));

        cloudWatchAsyncClientMockedStatic.verify(CloudWatchAsyncClient::create, never());
    }

    @Test
    public void testDisposeCloudWatchAsyncClient() {
        sut.createObject();
        applicationDisposer.dispose();
        
        verify(cloudWatchAsyncClient).close();
    }
}