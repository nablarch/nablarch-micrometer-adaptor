package nablarch.integration.micrometer.instrument.http;

import io.micrometer.core.instrument.Tag;
import jakarta.servlet.http.HttpServletResponse;
import nablarch.fw.handler.MethodBinding;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.servlet.ServletExecutionContext;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpRequestTimeMetricsMetaDataBuilderTest {
    private static Object RESULT = null;

    private final HttpRequest request = mock(HttpRequest.class);
    private final ServletExecutionContext context = mock(ServletExecutionContext.class);
    private final HttpServletResponse servletResponse = mock(HttpServletResponse.class);

    private HttpRequestTimeMetricsMetaDataBuilder sut = new HttpRequestTimeMetricsMetaDataBuilder();

    @Test
    public void testGetMetricsNameInDefault() {
        assertThat(sut.getMetricsName(), is(HttpRequestTimeMetricsMetaDataBuilder.DEFAULT_METRICS_NAME));
    }

    @Test
    public void testGetMetricsDescriptionInDefault() {
        assertThat(sut.getMetricsDescription(), is(HttpRequestTimeMetricsMetaDataBuilder.DEFAULT_METRICS_DESCRIPTION));
    }

    @Test
    public void testSetMetricsName() {
        sut.setMetricsName("test.metrics");
        assertThat(sut.getMetricsName(), is("test.metrics"));
    }

    @Test
    public void testSetMetricsDescription() {
        sut.setMetricsDescription("Test description.");
        assertThat(sut.getMetricsDescription(), is("Test description."));
    }

    @Test
    public void testStandardCase() {
        when(context.getServletResponse()).thenReturn(servletResponse);
        when(context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS)).thenReturn(TestController.class);
        when(context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_METHOD)).thenReturn(TestController.ACTION_METHOD_WITHOUT_ARGS);

        when(request.getMethod()).thenReturn("GET");
        when(servletResponse.getStatus()).thenReturn(200);

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
        when(context.getServletResponse()).thenReturn(servletResponse);
        when(context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS)).thenReturn(TestController.class);
        when(context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_METHOD)).thenReturn(TestController.ACTION_METHOD_WITH_ARGS);

        when(request.getMethod()).thenReturn("GET");
        when(servletResponse.getStatus()).thenReturn(404);

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
        when(context.getServletResponse()).thenReturn(servletResponse);
        when(context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS)).thenReturn(TestController.class);
        when(context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_METHOD)).thenReturn(TestController.ACTION_METHOD_WITHOUT_ARGS);

        when(request.getMethod()).thenReturn("POST");
        when(servletResponse.getStatus()).thenReturn(500);

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
        when(context.getServletResponse()).thenReturn(servletResponse);
        when(context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS)).thenReturn(TestController.class);
        when(context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_METHOD)).thenReturn(TestController.ACTION_METHOD_WITHOUT_ARGS);
        when(context.getException()).thenReturn(new IllegalArgumentException("test"), null);

        when(request.getMethod()).thenReturn("PUT");
        when(servletResponse.getStatus()).thenReturn(500);

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
        when(context.getServletResponse()).thenReturn(servletResponse);
        when(context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS)).thenReturn(null);
        when(context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_METHOD)).thenReturn(null);

        when(request.getMethod()).thenReturn("GET");
        when(servletResponse.getStatus()).thenReturn(200);

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
        when(context.getServletResponse()).thenReturn(servletResponse);
        when(context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS)).thenReturn(TestController.class);
        when(context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_METHOD)).thenReturn(TestController.ACTION_METHOD_ARRAY);

        when(request.getMethod()).thenReturn("GET");
        when(servletResponse.getStatus()).thenReturn(200);

        List<Tag> tagList = sut.buildTagList(request, context, RESULT, null);

        assertThat(tagList, hasItem(Tag.of("method", "array_java.lang.String[]")));
    }

    @Test
    public void testMethodArgumentFormatNestedArray() {
        when(context.getServletResponse()).thenReturn(servletResponse);
        when(context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS)).thenReturn(TestController.class);
        when(context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_METHOD)).thenReturn(TestController.ACTION_METHOD_NESTED_ARRAY);

        when(request.getMethod()).thenReturn("GET");
        when(servletResponse.getStatus()).thenReturn(200);

        List<Tag> tagList = sut.buildTagList(request, context, RESULT, null);

        assertThat(tagList, hasItem(Tag.of("method", "nestedArray_java.lang.String[][]")));
    }

    @Test
    public void testMethodArgumentFormatMemberClass() {
        when(context.getServletResponse()).thenReturn(servletResponse);
        when(context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS)).thenReturn(TestController.class);
        when(context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_METHOD)).thenReturn(TestController.ACTION_METHOD_MEMBER_CLASS);

        when(request.getMethod()).thenReturn("GET");
        when(servletResponse.getStatus()).thenReturn(200);

        List<Tag> tagList = sut.buildTagList(request, context, RESULT, null);

        assertThat(tagList, hasItem(Tag.of("method", "memberClass_nablarch.integration.micrometer.instrument.http.TestController.MemberClass")));
    }

    @Test
    public void testClassFormatMemberClass() {
        when(context.getServletResponse()).thenReturn(servletResponse);
        when(context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_CLASS)).thenReturn(TestController.MemberController.class);
        when(context.getRequestScopedVar(MethodBinding.SCOPE_VAR_NAME_BOUND_METHOD)).thenReturn(TestController.MemberController.ACTION_METHOD);

        when(request.getMethod()).thenReturn("GET");
        when(servletResponse.getStatus()).thenReturn(200);

        List<Tag> tagList = sut.buildTagList(request, context, RESULT, null);

        assertThat(tagList, hasItem(Tag.of("class", "nablarch.integration.micrometer.instrument.http.TestController$MemberController")));
    }
}