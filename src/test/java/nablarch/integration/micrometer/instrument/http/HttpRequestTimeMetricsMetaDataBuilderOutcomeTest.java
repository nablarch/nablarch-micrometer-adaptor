package nablarch.integration.micrometer.instrument.http;

import io.micrometer.core.instrument.Tag;
import mockit.Expectations;
import mockit.Mocked;
import nablarch.fw.handler.MethodBinding;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.servlet.ServletExecutionContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

/**
 * {@link HttpRequestTimeMetricsMetaDataBuilder}の outcome タグのパターンをテストするクラス。
 * @author Tanaka Tomoyuki
 */
@RunWith(Parameterized.class)
public class HttpRequestTimeMetricsMetaDataBuilderOutcomeTest {
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

    public HttpRequestTimeMetricsMetaDataBuilderOutcomeTest(Fixture fixture) {
        this.fixture = fixture;
    }

    @Test
    public void test() {
        HttpRequestTimeMetricsMetaDataBuilder sut = new HttpRequestTimeMetricsMetaDataBuilder();

        new Expectations() {{
            context.getServletResponse(); result = httpServletResponse;

            context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS); result = TestController.class;
            context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_METHOD); result = TestController.ACTION_METHOD_WITHOUT_ARGS;
            request.getMethod(); result = "PUT";
            httpServletResponse.getStatus(); result = fixture.statusCode;
        }};

        List<Tag> tagList = sut.buildTagList(request, context, null, null);
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
