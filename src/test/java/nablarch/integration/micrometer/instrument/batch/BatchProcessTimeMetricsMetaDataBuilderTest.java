package nablarch.integration.micrometer.instrument.batch;

import io.micrometer.core.instrument.Tag;
import mockit.Expectations;
import mockit.Mocked;
import nablarch.fw.ExecutionContext;
import nablarch.fw.launcher.CommandLine;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

/**
 * {@link BatchProcessTimeMetricsMetaDataBuilder} の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class BatchProcessTimeMetricsMetaDataBuilderTest {
    @Mocked
    private CommandLine commandLine;

    private BatchProcessTimeMetricsMetaDataBuilder sut = new BatchProcessTimeMetricsMetaDataBuilder();
    private ExecutionContext context = new ExecutionContext();

    @Test
    public void testGetMetricsName() {
        assertThat(sut.getMetricsName(), is("batch.process.time"));
    }

    @Test
    public void testGetMetricsDescription() {
        assertThat(sut.getMetricsDescription(), is("Batch process time."));
    }

    @Test
    public void testBuildTagList() {
        new Expectations() {{
            commandLine.getRequestPath(); result = "HelloAction/test";
        }};

        Object handlerResult = new Object() {
            @Override
            public String toString() {
                return "TEST";
            }
        };

        List<Tag> result = sut.buildTagList(commandLine, context, handlerResult, null);

        assertThat(result, containsInAnyOrder(
            Tag.of("class", "HelloAction"),
            Tag.of("status", "TEST")
        ));
    }

    @Test
    public void testBuildTagListIfHandlerResultIsNull() {
        new Expectations() {{
            commandLine.getRequestPath(); result = "HelloAction/test";
        }};

        List<Tag> result = sut.buildTagList(commandLine, context, null, new Throwable());

        assertThat(result, containsInAnyOrder(
            Tag.of("class", "HelloAction"),
            Tag.of("status", "None")
        ));
    }
}