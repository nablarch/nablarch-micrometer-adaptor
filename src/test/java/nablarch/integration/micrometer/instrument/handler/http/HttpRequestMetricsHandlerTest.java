package nablarch.integration.micrometer.instrument.handler.http;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import mockit.Expectations;
import mockit.Mocked;
import nablarch.fw.web.HttpRequest;
import nablarch.fw.web.HttpResponse;
import nablarch.fw.web.servlet.ServletExecutionContext;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

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
    private ServletExecutionContext context;
    @Mocked
    private HttpRequestMetricsTagBuilder tagBuilder;

    private HttpRequestMetricsHandler sut;
    private SimpleMeterRegistry registry;

    @Before
    public void setUp() {
        sut = new HttpRequestMetricsHandler();
        sut.setHttpRequestMetricsTagBuilder(tagBuilder);
        registry = new SimpleMeterRegistry();
        sut.setMeterRegistry(registry);
    }

    @Test
    public void testInvokeNextHandler() {
        new Expectations() {{
            context.handleNext(request); result = response;
        }};

        HttpResponse response = sut.handle(request, context);

        assertThat(response, is(sameInstance(response)));
    }

    @Test
    public void testMeasureTime() {
        new Expectations() {{
            context.handleNext(request); result = response;

            tagBuilder.build(request, context, null);
            result = Arrays.asList(Tag.of("foo", "FOO"), Tag.of("bar", "BAR"));
        }};

        sut.handle(request, context);

        Timer timer = registry.get("http.server.requests").timer();
        assertThat(timer.count(), is(1L));

        Meter.Id id = timer.getId();
        assertThat(id.getTag("foo"), is("FOO"));
        assertThat(id.getTag("bar"), is("BAR"));
    }

    @Test
    public void testTimerThrowsExceptionCase() {
        new Expectations() {{
            context.handleNext(request); result = new Throwable("test throwable");
        }};

        Throwable throwable = assertThrows(Throwable.class, () -> sut.handle(request, context));
        assertThat(throwable.getMessage(), is("test throwable"));

        Timer timer = registry.get("http.server.requests").timer();
        assertThat(timer.count(), is(1L));
    }

    @Test
    public void testThrowsExceptionIfMeterRegistryIsNull() {
        sut.setMeterRegistry(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sut.handle(request, context));
        assertThat(exception.getMessage(), is("meterRegistry is not set."));
    }
}