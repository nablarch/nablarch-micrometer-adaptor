package nablarch.integration.micrometer.instrument.handler.http;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import mockit.Expectations;
import mockit.Mocked;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.servlet.ServletExecutionContext;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThrows;

/**
 * {@link HttpRequestMetricsHandler}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class HttpRequestMetricsHandlerTest {
    @Mocked
    private HttpRequest request;
    @Mocked
    private HttpResponse response;
    @Mocked
    private HttpServletResponse httpServletResponse;
    @Mocked
    private ServletExecutionContext context;

    private HttpRequestMetricsHandler sut;
    private SimpleMeterRegistry registry;

    @Before
    public void setUp() {
        sut = new HttpRequestMetricsHandler();
        registry = new SimpleMeterRegistry();
        sut.setMeterRegistry(registry);
    }

    private void setupNormalCaseExpectation() {
        new Expectations() {{
            context.handleNext(request); result = response;
            context.getServletResponse(); result = httpServletResponse;
            context.getRequestScopedVar(HttpRequestMetricsHandler.DEFAULT_REQUEST_MAPPING_CLASS_VAR_NAME); result = "foo.bar.TestController";
            context.getRequestScopedVar(HttpRequestMetricsHandler.DEFAULT_REQUEST_MAPPING_METHOD_VAR_NAME); result = "hello";

            request.getMethod(); result = "GET";
            httpServletResponse.getStatus(); result = 200;
        }};
    }

    @Test
    public void testInvokeNextHandler() {
        setupNormalCaseExpectation();

        HttpResponse response = sut.handle(request, context);

        assertThat(response, is(sameInstance(response)));
    }

    @Test
    public void testTimerNormalCase() {
        setupNormalCaseExpectation();

        sut.handle(request, context);

        Timer timer = registry.get("http.server.requests").timer();
        Meter.Id id = timer.getId();

        assertThat(id.getTag("class"), is("foo.bar.TestController"));
        assertThat(id.getTag("method"), is("hello"));
        assertThat(id.getTag("httpMethod"), is("GET"));
        assertThat(id.getTag("status"), is("200"));
        assertThat(id.getTag("outcome"), is("SUCCESS"));
        assertThat(id.getTag("exception"), is("None"));
        assertThat(timer.count(), is(1L));
    }

    @Test
    public void testTimerThrowsExceptionCase() {
        new Expectations() {{
            context.handleNext(request); result = new Throwable("test throwable");
            context.getServletResponse(); result = httpServletResponse;

            request.getMethod(); result = "POST";
            context.getRequestScopedVar(HttpRequestMetricsHandler.DEFAULT_REQUEST_MAPPING_CLASS_VAR_NAME); result = "foo.bar.TestErrorController";
            context.getRequestScopedVar(HttpRequestMetricsHandler.DEFAULT_REQUEST_MAPPING_METHOD_VAR_NAME); result = "throwError";
            httpServletResponse.getStatus(); result = 500;
        }};

        Throwable throwable = assertThrows(Throwable.class, () -> sut.handle(request, context));
        assertThat(throwable.getMessage(), is("test throwable"));

        Timer timer = registry.get("http.server.requests").timer();
        Meter.Id id = timer.getId();

        assertThat(id.getTag("class"), is("foo.bar.TestErrorController"));
        assertThat(id.getTag("method"), is("throwError"));
        assertThat(id.getTag("httpMethod"), is("POST"));
        assertThat(id.getTag("status"), is("500"));
        assertThat(id.getTag("outcome"), is("SERVER_ERROR"));
        assertThat(id.getTag("exception"), is("Throwable"));
        assertThat(timer.count(), is(1L));
    }

    @Test
    public void testTimerReturnsErrorResponseCase() {
        new Expectations() {{
            context.handleNext(request); result = response;
            context.getServletResponse(); result = httpServletResponse;
            context.getException(); returns(new NullPointerException("test null"), null);

            request.getMethod(); result = "PUT";
            context.getRequestScopedVar(HttpRequestMetricsHandler.DEFAULT_REQUEST_MAPPING_CLASS_VAR_NAME); result = "foo.bar.TestController";
            context.getRequestScopedVar(HttpRequestMetricsHandler.DEFAULT_REQUEST_MAPPING_METHOD_VAR_NAME); result = "error";
            httpServletResponse.getStatus(); result = 500;
        }};

        sut.handle(request, context);

        Timer timer = registry.get("http.server.requests").timer();
        Meter.Id id = timer.getId();

        assertThat(id.getTag("class"), is("foo.bar.TestController"));
        assertThat(id.getTag("method"), is("error"));
        assertThat(id.getTag("httpMethod"), is("PUT"));
        assertThat(id.getTag("status"), is("500"));
        assertThat(id.getTag("outcome"), is("SERVER_ERROR"));
        assertThat(id.getTag("exception"), is("NullPointerException"));
        assertThat(timer.count(), is(1L));
    }

    @Test
    public void testClassAndMethodIsUnknown() {
        new Expectations() {{
            context.handleNext(request); result = response;
            context.getServletResponse(); result = httpServletResponse;

            request.getMethod(); result = "GET";
            context.getRequestScopedVar(HttpRequestMetricsHandler.DEFAULT_REQUEST_MAPPING_CLASS_VAR_NAME); result = null;
            context.getRequestScopedVar(HttpRequestMetricsHandler.DEFAULT_REQUEST_MAPPING_METHOD_VAR_NAME); result = null;
        }};

        sut.handle(request, context);

        Timer timer = registry.get("http.server.requests").timer();
        Meter.Id id = timer.getId();

        assertThat(id.getTag("class"), is("UNKNOWN"));
        assertThat(id.getTag("method"), is("UNKNOWN"));
    }

    @Test
    public void testChangeControllerAndActionVarNames() {
        new Expectations() {{
            context.handleNext(request); result = response;
            context.getServletResponse(); result = httpServletResponse;

            request.getMethod(); result = "GET";
            context.getRequestScopedVar("test_class_name"); result = "foo.bar.TestAction";
            context.getRequestScopedVar("test_method_name"); result = "execute";
        }};

        sut.setRequestMappingClassVarName("test_class_name");
        sut.setRequestMappingMethodVarName("test_method_name");
        sut.handle(request, context);

        Timer timer = registry.get("http.server.requests").timer();
        Meter.Id id = timer.getId();

        assertThat(id.getTag("class"), is("foo.bar.TestAction"));
        assertThat(id.getTag("method"), is("execute"));
    }

    @Test
    public void testThrowsExceptionIfMeterRegistryIsNull() {
        sut.setMeterRegistry(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sut.handle(request, context));
        assertThat(exception.getMessage(), is("meterRegistry is not set."));
    }
}