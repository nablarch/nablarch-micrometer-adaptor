package nablarch.integration.micrometer.cloudwatch;

import nablarch.core.util.annotation.Published;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

/**
 * {@link CloudWatchAsyncClient}のインスタンスを提供するインターフェース。
 * @author Tanaka Tomoyuki
 */
@Published(tag = "architect")
public interface CloudWatchAsyncClientProvider {

    /**
     * {@link CloudWatchAsyncClient}のインスタンスを提供する。
     * @return {@link CloudWatchAsyncClient}
     */
    CloudWatchAsyncClient provide();
}
