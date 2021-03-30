package nablarch.integration.micrometer.instrument;

import io.micrometer.core.instrument.Tag;
import nablarch.integration.micrometer.instrument.binder.MetricsMetaData;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * {@link MetricsMetaData}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class MetricsMetaDataTest {

    @Test
    public void testConstructorWithTags() {
        MetricsMetaData sut = new MetricsMetaData(
                "name", "description", Arrays.asList(Tag.of("foo", "FOO"), Tag.of("bar", "BAR")));

        assertThat(sut.getName(), is("name"));
        assertThat(sut.getDescription(), is("description"));
        assertThat(sut.getTags(), containsInAnyOrder(Tag.of("foo", "FOO"), Tag.of("bar", "BAR")));
    }

    @Test
    public void testConstructorWithoutTags() {
        MetricsMetaData sut = new MetricsMetaData("NAME", "DESCRIPTION");

        assertThat(sut.getName(), is("NAME"));
        assertThat(sut.getDescription(), is("DESCRIPTION"));
        assertThat(sut.getTags(), is(emptyIterable()));
    }
}