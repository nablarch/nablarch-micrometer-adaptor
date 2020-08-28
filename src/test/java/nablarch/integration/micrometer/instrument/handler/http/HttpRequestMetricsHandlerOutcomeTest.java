package nablarch.integration.micrometer.instrument.handler.http;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import mockit.Expectations;
import mockit.Mocked;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.servlet.ServletExecutionContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * {@link HttpRequestMetricsHandler}の outcome タグのパターンをテストするクラス。
 * @author Tanaka Tomoyuki
 */
@RunWith(Parameterized.class)
public class HttpRequestMetricsHandlerOutcomeTest {
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
    private HttpResponse response;
    @Mocked
    private HttpServletResponse httpServletResponse;
    @Mocked
    private ServletExecutionContext context;

    private final Fixture fixture;

    public HttpRequestMetricsHandlerOutcomeTest(Fixture fixture) {
        this.fixture = fixture;
    }

    @Test
    public void test() {
        HttpRequestMetricsHandler sut = new HttpRequestMetricsHandler();

        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        sut.setMeterRegistry(registry);

        new Expectations() {{
            context.handleNext(request); result = response;
            context.getServletResponse(); result = httpServletResponse;

            request.getMethod(); result = "PUT";
            request.getRequestPath(); result = "/test/path";
            httpServletResponse.getStatus(); result = fixture.statusCode;
        }};

        sut.handle(request, context);

        Meter.Id id = registry.get("http.server.requests").timer().getId();
        assertThat("statusCode=" + fixture.statusCode, id.getTag("outcome"), is(fixture.expectedOutcome));
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
