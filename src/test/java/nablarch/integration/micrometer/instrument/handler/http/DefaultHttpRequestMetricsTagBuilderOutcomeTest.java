package nablarch.integration.micrometer.instrument.handler.http;

import io.micrometer.core.instrument.Tag;
import mockit.Expectations;
import mockit.Mocked;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.servlet.ServletExecutionContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

/**
 * {@link DefaultHttpRequestMetricsTagBuilder}の outcome タグのパターンをテストするクラス。
 * @author Tanaka Tomoyuki
 */
@RunWith(Parameterized.class)
public class DefaultHttpRequestMetricsTagBuilderOutcomeTest {
    @Parameterized.Parameters
    public static List<Fixture> parameters() {
        return Arrays.asList(
            fixture(99, "UNKNOWN"),
            fixture(100, "INFORMATION"),
            fixture(199, "INFORMATION"),
            fixture(200, "SUCCESS"),
            fixture(299, "SUCCESS"),
            fixture(300, "REDIRECTION"),
            fixture(399, "REDIRECTION"),
            fixture(400, "CLIENT_ERROR"),
            fixture(499, "CLIENT_ERROR"),
            fixture(500, "SERVER_ERROR"),
            fixture(599, "SERVER_ERROR"),
            fixture(600, "UNKNOWN")
        );
    }

    @Mocked
    private HttpRequest request;
    @Mocked
    private HttpServletResponse httpServletResponse;
    @Mocked
    private ServletExecutionContext context;

    private final Fixture fixture;

    public DefaultHttpRequestMetricsTagBuilderOutcomeTest(Fixture fixture) {
        this.fixture = fixture;
    }

    @Test
    public void test() {
        DefaultHttpRequestMetricsTagBuilder sut = new DefaultHttpRequestMetricsTagBuilder();

        new Expectations() {{
            context.getServletResponse(); result = httpServletResponse;

            context.getRequestScopedVar(DefaultHttpRequestMetricsTagBuilder.DEFAULT_REQUEST_MAPPING_CLASS_VAR_NAME); result = "foo.bar.TestController";
            context.getRequestScopedVar(DefaultHttpRequestMetricsTagBuilder.DEFAULT_REQUEST_MAPPING_METHOD_VAR_NAME); result = "test";
            request.getMethod(); result = "PUT";
            httpServletResponse.getStatus(); result = fixture.statusCode;
        }};

        List<Tag> tagList = sut.build(request, context, null);
        assertThat("statusCode=" + fixture.statusCode, tagList, hasItem(Tag.of("outcome", fixture.expectedOutcome)));
    }

    private static Fixture fixture(int statusCode, String expectedOutcome) {
        return new Fixture(statusCode, expectedOutcome);
    }

    private static class Fixture {
        private final int statusCode;
        private final String expectedOutcome;

        private Fixture(int statusCode, String expectedOutcome) {
            this.statusCode = statusCode;
            this.expectedOutcome = expectedOutcome;
        }
    }
}
