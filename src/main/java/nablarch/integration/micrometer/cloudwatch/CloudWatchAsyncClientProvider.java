package nablarch.integration.micrometer.cloudwatch;

import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

/**
 * {@link CloudWatchAsyncClient}のインスタンスを提供するインターフェース。
 * @author Tanaka Tomoyuki
 */
public interface CloudWatchAsyncClientProvider {

    /**
     * {@link CloudWatchAsyncClient}のインスタンスを提供する。
     * @return {@link CloudWatchAsyncClient}
     */
    CloudWatchAsyncClient provide();
}
