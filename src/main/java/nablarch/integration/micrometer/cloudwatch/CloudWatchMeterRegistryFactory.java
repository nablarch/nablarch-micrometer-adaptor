package nablarch.integration.micrometer.cloudwatch;

import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import nablarch.integration.micrometer.MeterRegistryFactory;
import nablarch.integration.micrometer.MicrometerConfiguration;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

/**
 * {@link CloudWatchMeterRegistry}のファクトリ。
 * @author Tanaka Tomoyuki
 */
public class CloudWatchMeterRegistryFactory extends MeterRegistryFactory<CloudWatchMeterRegistry> {

    private CloudWatchAsyncClientProvider cloudWatchAsyncClientProvider = CloudWatchAsyncClient::create;

    @Override
    public CloudWatchMeterRegistry createObject() {
        return doCreateObject();
    }

    @Override
    protected CloudWatchMeterRegistry createMeterRegistry(MicrometerConfiguration micrometerConfiguration) {
        NablarchCloudWatchConfig cloudWatchConfig = new NablarchCloudWatchConfig(prefix, micrometerConfiguration);

        CloudWatchAsyncClient client = cloudWatchAsyncClientProvider.provide();
        applicationDisposer.addDisposable(client::close);

        return new CloudWatchMeterRegistry(cloudWatchConfig, Clock.SYSTEM, client);
    }

    /**
     * {@link CloudWatchAsyncClientProvider}を設定する。
     * @param cloudWatchAsyncClientProvider {@link CloudWatchAsyncClientProvider}
     */
    public void setCloudWatchAsyncClientProvider(CloudWatchAsyncClientProvider cloudWatchAsyncClientProvider) {
        this.cloudWatchAsyncClientProvider = cloudWatchAsyncClientProvider;
    }
}
