package nablarch.integration.micrometer.instrument.handler.http;

import io.micrometer.core.instrument.Tag;
import mockit.Expectations;
import mockit.Mocked;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.servlet.ServletExecutionContext;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

/**
 * {@link DefaultHttpRequestMetricsTagBuilder}の単体テスト。
 */
public class DefaultHttpRequestMetricsTagBuilderTest {
    @Mocked
    private HttpRequest request;
    @Mocked
    private ServletExecutionContext context;
    @Mocked
    private HttpServletResponse servletResponse;

    private DefaultHttpRequestMetricsTagBuilder sut = new DefaultHttpRequestMetricsTagBuilder();

    @Before
    public void setUp() {
        new Expectations() {{
            context.getServletResponse(); result = servletResponse;
        }};
    }

    @Test
    public void testStandardCase() {
        new Expectations() {{
            context.getRequestScopedVar(DefaultHttpRequestMetricsTagBuilder.DEFAULT_REQUEST_MAPPING_CLASS_VAR_NAME); result = "foo.bar.TestController";
            context.getRequestScopedVar(DefaultHttpRequestMetricsTagBuilder.DEFAULT_REQUEST_MAPPING_METHOD_VAR_NAME); result = "hello";

            request.getMethod(); result = "GET";
            servletResponse.getStatus(); result = 200;
        }};

        List<Tag> tagList = sut.build(request, context, null);

        assertThat(tagList, containsInAnyOrder(
            Tag.of("class", "foo.bar.TestController"),
            Tag.of("method", "hello"),
            Tag.of("httpMethod", "GET"),
            Tag.of("status", "200"),
            Tag.of("outcome", "SUCCESS"),
            Tag.of("exception", "None")
        ));
    }

    @Test
    public void testThrownThrowableIsNotNull() {
        new Expectations() {{
            context.getRequestScopedVar(DefaultHttpRequestMetricsTagBuilder.DEFAULT_REQUEST_MAPPING_CLASS_VAR_NAME); result = "foo.bar.TestController";
            context.getRequestScopedVar(DefaultHttpRequestMetricsTagBuilder.DEFAULT_REQUEST_MAPPING_METHOD_VAR_NAME); result = "throwable";

            request.getMethod(); result = "POST";
            servletResponse.getStatus(); result = 500;
        }};

        List<Tag> tagList = sut.build(request, context, new NullPointerException("test"));

        assertThat(tagList, containsInAnyOrder(
            Tag.of("class", "foo.bar.TestController"),
            Tag.of("method", "throwable"),
            Tag.of("httpMethod", "POST"),
            Tag.of("status", "500"),
            Tag.of("outcome", "SERVER_ERROR"),
            Tag.of("exception", "NullPointerException")
        ));
    }

    @Test
    public void testThrownThrowableIsNullButContextHasException() {
        new Expectations() {{
            context.getRequestScopedVar(DefaultHttpRequestMetricsTagBuilder.DEFAULT_REQUEST_MAPPING_CLASS_VAR_NAME); result = "foo.bar.TestController";
            context.getRequestScopedVar(DefaultHttpRequestMetricsTagBuilder.DEFAULT_REQUEST_MAPPING_METHOD_VAR_NAME); result = "exception";
            context.getException(); returns(new IllegalArgumentException("test"), null);

            request.getMethod(); result = "PUT";
            servletResponse.getStatus(); result = 500;
        }};

        List<Tag> tagList = sut.build(request, context, null);

        assertThat(tagList, containsInAnyOrder(
                Tag.of("class", "foo.bar.TestController"),
                Tag.of("method", "exception"),
                Tag.of("httpMethod", "PUT"),
                Tag.of("status", "500"),
                Tag.of("outcome", "SERVER_ERROR"),
                Tag.of("exception", "IllegalArgumentException")
        ));
    }

    @Test
    public void testStatusIsNotFound() {
        new Expectations() {{
            request.getMethod(); result = "GET";
            servletResponse.getStatus(); result = 404;
        }};

        List<Tag> tagList = sut.build(request, context, null);

        assertThat(tagList, containsInAnyOrder(
                Tag.of("class", "UNKNOWN"),
                Tag.of("method", "UNKNOWN"),
                Tag.of("httpMethod", "GET"),
                Tag.of("status", "404"),
                Tag.of("outcome", "CLIENT_ERROR"),
                Tag.of("exception", "None")
        ));
    }

    @Test
    public void testClassAndMethodAreNull() {
        new Expectations() {{
            context.getRequestScopedVar(DefaultHttpRequestMetricsTagBuilder.DEFAULT_REQUEST_MAPPING_CLASS_VAR_NAME); result = null;
            context.getRequestScopedVar(DefaultHttpRequestMetricsTagBuilder.DEFAULT_REQUEST_MAPPING_METHOD_VAR_NAME); result = null;

            request.getMethod(); result = "GET";
            servletResponse.getStatus(); result = 200;
        }};

        List<Tag> tagList = sut.build(request, context, null);

        assertThat(tagList, containsInAnyOrder(
                Tag.of("class", "UNKNOWN"),
                Tag.of("method", "UNKNOWN"),
                Tag.of("httpMethod", "GET"),
                Tag.of("status", "200"),
                Tag.of("outcome", "SUCCESS"),
                Tag.of("exception", "None")
        ));
    }

    @Test
    public void testChangeClassAndMethodVarNames() {
        new Expectations() {{
            context.getRequestScopedVar("test_class_name"); result = "fizz.buzz.TestController";
            context.getRequestScopedVar("test_method_name"); result = "execute";

            request.getMethod(); result = "GET";
            servletResponse.getStatus(); result = 200;
        }};

        sut.setRequestMappingClassVarName("test_class_name");
        sut.setRequestMappingMethodVarName("test_method_name");
        List<Tag> tagList = sut.build(request, context, null);

        assertThat(tagList, containsInAnyOrder(
                Tag.of("class", "fizz.buzz.TestController"),
                Tag.of("method", "execute"),
                Tag.of("httpMethod", "GET"),
                Tag.of("status", "200"),
                Tag.of("outcome", "SUCCESS"),
                Tag.of("exception", "None")
        ));
    }
}