package nablarch.integration.micrometer.instrument.http;

import io.micrometer.core.instrument.Tag;
import mockit.Expectations;
import mockit.Mocked;
import nablarch.fw.handler.MethodBinding;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.servlet.ServletExecutionContext;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class HttpRequestTimeMetricsMetaDataBuilderTest {
    private static Object RESULT = null;

    @Mocked
    private HttpRequest request;
    @Mocked
    private ServletExecutionContext context;
    @Mocked
    private HttpServletResponse servletResponse;

    private HttpRequestTimeMetricsMetaDataBuilder sut = new HttpRequestTimeMetricsMetaDataBuilder();

    @Test
    public void testGetMetricsName() {
        assertThat(sut.getMetricsName(), is("http.server.requests"));
    }

    @Test
    public void testGetMetricsDescription() {
        assertThat(sut.getMetricsDescription(), is("HTTP request metrics measured by Timer."));
    }

    @Test
    public void testStandardCase() {
        new Expectations() {{
            context.getServletResponse(); result = servletResponse;
            context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS); result = TestController.class;
            context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_METHOD); result = TestController.ACTION_METHOD_WITHOUT_ARGS;

            request.getMethod(); result = "GET";
            servletResponse.getStatus(); result = 200;
        }};

        List<Tag> tagList = sut.buildTagList(request, context, RESULT, null);

        assertThat(tagList, containsInAnyOrder(
            Tag.of("class", TestController.class.getName()),
            Tag.of("method", TestController.ACTION_METHOD_WITHOUT_ARGS.getName()),
            Tag.of("httpMethod", "GET"),
            Tag.of("status", "200"),
            Tag.of("outcome", "SUCCESS"),
            Tag.of("exception", "None")
        ));
    }

    @Test
    public void testMethodHasArguments() {
        new Expectations() {{
            context.getServletResponse(); result = servletResponse;
            context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS); result = TestController.class;
            context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_METHOD); result = TestController.ACTION_METHOD_WITH_ARGS;

            request.getMethod(); result = "GET";
            servletResponse.getStatus(); result = 404;
        }};

        List<Tag> tagList = sut.buildTagList(request, context, RESULT, null);

        assertThat(tagList, containsInAnyOrder(
            Tag.of("class", TestController.class.getName()),
            Tag.of("method", "withArgs_int_java.lang.String"),
            Tag.of("httpMethod", "GET"),
            Tag.of("status", "404"),
            Tag.of("outcome", "CLIENT_ERROR"),
            Tag.of("exception", "None")
        ));
    }

    @Test
    public void testThrownThrowableIsNotNull() {
        new Expectations() {{
            context.getServletResponse(); result = servletResponse;
            context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS); result = TestController.class;
            context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_METHOD); result = TestController.ACTION_METHOD_WITHOUT_ARGS;

            request.getMethod(); result = "POST";
            servletResponse.getStatus(); result = 500;
        }};

        List<Tag> tagList = sut.buildTagList(request, context, RESULT, new NullPointerException("test"));

        assertThat(tagList, containsInAnyOrder(
            Tag.of("class", TestController.class.getName()),
            Tag.of("method", TestController.ACTION_METHOD_WITHOUT_ARGS.getName()),
            Tag.of("httpMethod", "POST"),
            Tag.of("status", "500"),
            Tag.of("outcome", "SERVER_ERROR"),
            Tag.of("exception", "NullPointerException")
        ));
    }

    @Test
    public void testThrownThrowableIsNullButContextHasException() {
        new Expectations() {{
            context.getServletResponse(); result = servletResponse;
            context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS); result = TestController.class;
            context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_METHOD); result = TestController.ACTION_METHOD_WITHOUT_ARGS;
            context.getException(); returns(new IllegalArgumentException("test"), null);

            request.getMethod(); result = "PUT";
            servletResponse.getStatus(); result = 500;
        }};

        List<Tag> tagList = sut.buildTagList(request, context, RESULT, null);

        assertThat(tagList, containsInAnyOrder(
            Tag.of("class", TestController.class.getName()),
            Tag.of("method", TestController.ACTION_METHOD_WITHOUT_ARGS.getName()),
            Tag.of("httpMethod", "PUT"),
            Tag.of("status", "500"),
            Tag.of("outcome", "SERVER_ERROR"),
            Tag.of("exception", "IllegalArgumentException")
        ));
    }

    @Test
    public void testClassAndMethodAreNull() {
        new Expectations() {{
            context.getServletResponse(); result = servletResponse;
            context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS); result = null;
            context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_METHOD); result = null;

            request.getMethod(); result = "GET";
            servletResponse.getStatus(); result = 200;
        }};

        List<Tag> tagList = sut.buildTagList(request, context, RESULT, null);

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
    public void testMethodArgumentFormatArray() {
        new Expectations() {{
            context.getServletResponse(); result = servletResponse;
            context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS); result = TestController.class;
            context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_METHOD); result = TestController.ACTION_METHOD_ARRAY;

            request.getMethod(); result = "GET";
            servletResponse.getStatus(); result = 200;
        }};

        List<Tag> tagList = sut.buildTagList(request, context, RESULT, null);

        assertThat(tagList, hasItem(Tag.of("method", "array_java.lang.String[]")));
    }

    @Test
    public void testMethodArgumentFormatNestedArray() {
        new Expectations() {{
            context.getServletResponse(); result = servletResponse;
            context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS); result = TestController.class;
            context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_METHOD); result = TestController.ACTION_METHOD_NESTED_ARRAY;

            request.getMethod(); result = "GET";
            servletResponse.getStatus(); result = 200;
        }};

        List<Tag> tagList = sut.buildTagList(request, context, RESULT, null);

        assertThat(tagList, hasItem(Tag.of("method", "nestedArray_java.lang.String[][]")));
    }

    @Test
    public void testMethodArgumentFormatMemberClass() {
        new Expectations() {{
            context.getServletResponse(); result = servletResponse;
            context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS); result = TestController.class;
            context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_METHOD); result = TestController.ACTION_METHOD_MEMBER_CLASS;

            request.getMethod(); result = "GET";
            servletResponse.getStatus(); result = 200;
        }};

        List<Tag> tagList = sut.buildTagList(request, context, RESULT, null);

        assertThat(tagList, hasItem(Tag.of("method", "memberClass_nablarch.integration.micrometer.instrument.http.TestController.MemberClass")));
    }

    @Test
    public void testClassFormatMemberClass() {
        new Expectations() {{
            context.getServletResponse(); result = servletResponse;
            context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS); result = TestController.MemberController.class;
            context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_METHOD); result = TestController.MemberController.ACTION_METHOD;

            request.getMethod(); result = "GET";
            servletResponse.getStatus(); result = 200;
        }};

        List<Tag> tagList = sut.buildTagList(request, context, RESULT, null);

        assertThat(tagList, hasItem(Tag.of("class", "nablarch.integration.micrometer.instrument.http.TestController$MemberController")));
    }
}