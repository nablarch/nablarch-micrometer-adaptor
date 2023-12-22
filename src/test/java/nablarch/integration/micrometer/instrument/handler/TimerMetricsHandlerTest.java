package nablarch.integration.micrometer.instrument.handler;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import nablarch.fw.web.servlet.ServletExecutionContext;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link TimerMetricsHandler} の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class TimerMetricsHandlerTest {
    private final ServletExecutionContext context = mock(ServletExecutionContext.class);
    @SuppressWarnings("unchecked")
    private final HandlerMetricsMetaDataBuilder<String, String> metricsInfoBuilder = mock(HandlerMetricsMetaDataBuilder.class);

    private static final String PARAM = "REQUEST";
    private static final String RESULT = "RESULT";
    private static final String METRICS_NAME = "test.handler.metrics";
    private static final String METRICS_DESCRIPTION = "Test metrics";

    private TimerMetricsHandler<String, String> sut;
    private SimpleMeterRegistry registry;

    @Before
    public void setUp() {
        sut = new TimerMetricsHandler<>();
        sut.setHandlerMetricsMetaDataBuilder(metricsInfoBuilder);

        registry = new SimpleMeterRegistry(SimpleConfig.DEFAULT, new Clock() {
            final Iterator<Long> times = Arrays.asList(1000L, 2500L).iterator();

            @Override
            public long monotonicTime() {
                return times.next();
            }

            @Override
            public long wallTime() {
                return 0;
            }
        });
        sut.setMeterRegistry(registry);
    }

    @Test
    public void testInvokeNextHandler() {
        when(metricsInfoBuilder.getMetricsName()).thenReturn(METRICS_NAME);
        when(context.handleNext(PARAM)).thenReturn(RESULT);

        String actualResult = sut.handle(PARAM, context);

        assertThat(actualResult, is(sameInstance(RESULT)));
    }

    @Test
    public void testMeasureTime() {
        when(context.handleNext(PARAM)).thenReturn(RESULT);

        when(metricsInfoBuilder.getMetricsName()).thenReturn(METRICS_NAME);
        when(metricsInfoBuilder.getMetricsDescription()).thenReturn(METRICS_DESCRIPTION);

        when(metricsInfoBuilder.buildTagList(PARAM, context, RESULT, null)).thenReturn(
                List.of(Tag.of("foo", "FOO"), Tag.of("bar", "BAR"))
        );

        sut.handle(PARAM, context);

        Timer timer = registry.get(METRICS_NAME).timer();
        assertThat(timer.count(), is(1L));
        assertThat(timer.totalTime(TimeUnit.NANOSECONDS), is(1500.0));

        Meter.Id id = timer.getId();
        assertThat(id.getTag("foo"), is("FOO"));
        assertThat(id.getTag("bar"), is("BAR"));
        assertThat(id.getDescription(), is(METRICS_DESCRIPTION));
    }

    @Test
    public void testMeasureTimeNextHandleThrowsExceptionCase() {
        RuntimeException exception = new RuntimeException("test exception");

        when(context.handleNext(PARAM)).thenThrow(exception);

        when(metricsInfoBuilder.getMetricsName()).thenReturn(METRICS_NAME);
        when(metricsInfoBuilder.getMetricsDescription()).thenReturn(METRICS_DESCRIPTION);

        when(metricsInfoBuilder.buildTagList(PARAM, context, null, exception))
                .thenReturn(List.of(Tag.of("fizz", "FIZZ"), Tag.of("buzz", "BUZZ")));

        RuntimeException throwable = assertThrows(RuntimeException.class, () -> sut.handle(PARAM, context));
        assertThat(throwable.getMessage(), is("test exception"));

        Timer timer = registry.get(METRICS_NAME).timer();
        assertThat(timer.count(), is(1L));
        assertThat(timer.totalTime(TimeUnit.NANOSECONDS), is(1500.0));

        Meter.Id id = timer.getId();
        assertThat(id.getTag("fizz"), is("FIZZ"));
        assertThat(id.getTag("buzz"), is("BUZZ"));
    }

    @Test
    public void testThrowsExceptionIfMetricsInfoBuilderIsNull() {
        sut.setHandlerMetricsMetaDataBuilder(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sut.handle(PARAM, context));

        assertThat(exception.getMessage(), is("handlerMetricsMetaDataBuilder is not set."));
    }

    @Test
    public void testThrowsExceptionIfMeterRegistryIsNull() {
        sut.setMeterRegistry(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sut.handle(PARAM, context));

        assertThat(exception.getMessage(), is("meterRegistry is not set."));
    }
}