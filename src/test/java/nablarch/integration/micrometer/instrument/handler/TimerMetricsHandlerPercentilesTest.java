package nablarch.integration.micrometer.instrument.handler;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.ValueAtPercentile;
import io.micrometer.registry.otlp.OtlpConfig;
import io.micrometer.registry.otlp.OtlpMeterRegistry;
import nablarch.fw.ExecutionContext;
import nablarch.fw.Handler;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * {@link TimerMetricsHandler}のパーセンタイルに関する設定のテストを行う単体テスト。
 * @author Tanaka Tomoyuki
 */
public class TimerMetricsHandlerPercentilesTest {
    private final ExecutionContext context = new ExecutionContext();

    private final TimerMetricsHandler<String, String> sut = new TimerMetricsHandler<>();

    private final OtlpMeterRegistry meterRegistry = new OtlpMeterRegistry(OtlpConfig.DEFAULT, Clock.SYSTEM);

    @Before
    public void setUp() {
        context.addHandler((Handler<String, String>) (param, context) -> "RESULT");

        sut.setMeterRegistry(meterRegistry);
        sut.setHandlerMetricsMetaDataBuilder(metricsMetaDataBuilder);
    }

    @Test
    public void testSetPercentiles() {
        sut.setPercentiles(Arrays.asList("0.95", "0.5"));

        sut.handle("PARAM", context);

        Timer timer = meterRegistry.find(metricsMetaDataBuilder.getMetricsName()).timer();

        assertThat(timer,notNullValue());
        ValueAtPercentile[] valueAtPercentiles = timer.takeSnapshot().percentileValues();
        assertThat(valueAtPercentiles, arrayWithSize(2));

        List<Double> percentiles = Stream.of(valueAtPercentiles).map(ValueAtPercentile::percentile).collect(Collectors.toList());
        assertThat(percentiles, containsInAnyOrder(0.95, 0.5));
    }

    @Test
    public void testSetEnablePercentileHistogramWhenDisable() {
        sut.setEnablePercentileHistogram(false);

        sut.handle("PARAM", context);

        Timer timer = meterRegistry.find(metricsMetaDataBuilder.getMetricsName()).timer();

        assertThat(timer,notNullValue());
        assertThat(timer.takeSnapshot().histogramCounts(), is(emptyArray()));
    }

    @Test
    public void testSetEnablePercentileHistogramWhenEnable() {
        sut.setEnablePercentileHistogram(true);

        sut.handle("PARAM", context);

        Timer timer = meterRegistry.find(metricsMetaDataBuilder.getMetricsName()).timer();

        assertThat(timer,notNullValue());
        assertThat(timer.takeSnapshot().histogramCounts(), is(not(emptyArray())));
    }

    @Test
    public void testSetServiceLevelObjectives() {
        sut.setEnablePercentileHistogram(true);
        sut.setServiceLevelObjectives(Arrays.asList("90000", "99000"));

        sut.handle("PARAM", context);

        Timer timer = meterRegistry.find(metricsMetaDataBuilder.getMetricsName()).timer();

        assertThat(timer,notNullValue());
        List<Double> buckets = Stream.of(timer.takeSnapshot().histogramCounts()).map(cab -> cab.bucket(TimeUnit.MILLISECONDS)).collect(Collectors.toList());
        assertThat(buckets, hasItems(90000.0, 99000.0));
    }

    @Test
    public void testSetMinimumExpectedValue() {
        sut.setEnablePercentileHistogram(true);
        sut.setMinimumExpectedValue(987L);

        sut.handle("PARAM", context);

        Timer timer = meterRegistry.find(metricsMetaDataBuilder.getMetricsName()).timer();

        assertThat(timer,notNullValue());
        List<Double> buckets = Stream.of(timer.takeSnapshot().histogramCounts()).map(cab -> cab.bucket(TimeUnit.MILLISECONDS)).collect(Collectors.toList());
        assertThat(buckets, hasItems(987.0));
    }

    @Test
    public void testSetMaximumExpectedValue() {
        sut.setEnablePercentileHistogram(true);
        sut.setMaximumExpectedValue(45678L);

        sut.handle("PARAM", context);

        Timer timer = meterRegistry.find(metricsMetaDataBuilder.getMetricsName()).timer();

        assertThat(timer,notNullValue());
        List<Double> buckets = Stream.of(timer.takeSnapshot().histogramCounts()).map(cab -> cab.bucket(TimeUnit.MILLISECONDS)).collect(Collectors.toList());
        assertThat(buckets, hasItems(45678.0));
    }

    private final HandlerMetricsMetaDataBuilder<String, String> metricsMetaDataBuilder = new HandlerMetricsMetaDataBuilder<String, String>() {
        @Override
        public String getMetricsName() {
            return "test.metrics";
        }

        @Override
        public String getMetricsDescription() {
            return "Test metrics.";
        }

        @Override
        public List<Tag> buildTagList(String param, ExecutionContext executionContext, String s, Throwable thrownThrowable) {
            return Collections.emptyList();
        }
    };
}