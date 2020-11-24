package nablarch.integration.micrometer.instrument.handler;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import mockit.Expectations;
import mockit.Mocked;
import nablarch.fw.web.servlet.ServletExecutionContext;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThrows;

/**
 * {@link TimerMetricsHandler} の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class TimerMetricsHandlerTest {
    @Mocked
    private ServletExecutionContext context;
    @Mocked
    private HandlerMetricsMetaDataBuilder<String, String> metricsInfoBuilder;

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
        new Expectations() {{
            metricsInfoBuilder.getMetricsName(); result = METRICS_NAME;
            context.handleNext(PARAM); result = RESULT;
        }};

        String actualResult = sut.handle(PARAM, context);

        assertThat(actualResult, is(sameInstance(RESULT)));
    }

    @Test
    public void testMeasureTime() {
        new Expectations() {{
            context.handleNext(PARAM); result = RESULT;

            metricsInfoBuilder.getMetricsName(); result = METRICS_NAME;
            metricsInfoBuilder.getMetricsDescription(); result = METRICS_DESCRIPTION;

            metricsInfoBuilder.buildTagList(PARAM, context, RESULT, null);
            result = Arrays.asList(Tag.of("foo", "FOO"), Tag.of("bar", "BAR"));
        }};

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
        Throwable thrown = new Throwable("test throwable");
        new Expectations() {{
            context.handleNext(PARAM); result = thrown;

            metricsInfoBuilder.getMetricsName(); result = METRICS_NAME;
            metricsInfoBuilder.getMetricsDescription(); result = METRICS_DESCRIPTION;

            metricsInfoBuilder.buildTagList(PARAM, context, null, thrown);
            result = Arrays.asList(Tag.of("fizz", "FIZZ"), Tag.of("buzz", "BUZZ"));
        }};

        Throwable throwable = assertThrows(Throwable.class, () -> sut.handle(PARAM, context));
        assertThat(throwable.getMessage(), is("test throwable"));

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