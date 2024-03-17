package nablarch.integration.micrometer.otlp;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.util.MeterPartition;
import io.micrometer.registry.otlp.OtlpMeterRegistry;
import software.amazon.awssdk.services.cloudwatch.model.Metric;

import java.util.List;

public class NablarchOtlpMeterRegistry extends OtlpMeterRegistry {
    @Override
    protected void publish() {
        for (List<Meter> batch : MeterPartition.partition(this, OtlpMeterRegistry.batchSize())) {
            List<Metric> metrics = batch.stream()
                    .map(meter -> meter.match(this::writeGauge, this::writeCounter, this::writeHistogramSupport,
                            this::writeHistogramSupport, this::writeHistogramSupport, this::writeGauge,
                            this::writeFunctionCounter, this::writeFunctionTimer, this::writeMeter))
                    .collect(Collectors.toList());
            try {
                ExportMetricsServiceRequest request = ExportMetricsServiceRequest.newBuilder()
                        .addResourceMetrics(ResourceMetrics.newBuilder().setResource(this.resource)
                                .addScopeMetrics(ScopeMetrics.newBuilder()
                                        // we don't have instrumentation library/version
                                        // attached to meters; leave unknown for now
                                        // .setScope(InstrumentationScope.newBuilder().setName("").setVersion("").build())
                                        .addAllMetrics(metrics).build())
                                .build())
                        .build();
                this.httpSender.post(this.config.url()).withContent("application/x-protobuf", request.toByteArray())
                        .send();
                HttpSender.Request.Builder httpRequest = this.httpSender.post(this.config.url())
                        .withContent("application/x-protobuf", request.toByteArray());
                this.config.headers().forEach(httpRequest::withHeader);
                httpRequest.send();
            }
            catch (Throwable e) {
                logger.warn("Failed to publish metrics to OTLP receiver", e);
            }
        }
    }
}
