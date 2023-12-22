package nablarch.integration.micrometer.instrument.http;

import io.micrometer.core.instrument.Tag;
import jakarta.servlet.http.HttpServletResponse;
import nablarch.fw.handler.MethodBinding;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.servlet.ServletExecutionContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    private final HttpRequest request = mock(HttpRequest.class);
    private final HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
    private final ServletExecutionContext context = mock(ServletExecutionContext.class);

    private final Fixture fixture;

    public HttpRequestTimeMetricsMetaDataBuilderOutcomeTest(Fixture fixture) {
        this.fixture = fixture;
    }

    @Test
    public void test() {
        HttpRequestTimeMetricsMetaDataBuilder sut = new HttpRequestTimeMetricsMetaDataBuilder();

        when(context.getServletResponse()).thenReturn(httpServletResponse);

        when(context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS)).thenReturn(TestController.class);
        when(context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_METHOD)).thenReturn(TestController.ACTION_METHOD_WITHOUT_ARGS);

        when(request.getMethod()).thenReturn("PUT");
        when(httpServletResponse.getStatus()).thenReturn(fixture.statusCode);

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
