package nablarch.integration.micrometer.instrument.batch;

import io.micrometer.core.instrument.Tag;
import nablarch.integration.micrometer.instrument.batch.BatchActionClassTagUtil;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * {@link BatchActionClassTagUtil} の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class BatchActionClassTagUtilTest {

    @Test
    public void testObtain() {
        Tag result = BatchActionClassTagUtil.obtain("BatchActionClass/request-id");

        assertThat(result.getKey(), is("class"));
        assertThat(result.getValue(), is("BatchActionClass"));
    }

    @Test
    public void testThrowsExceptionIfRequestPathFormatIsIllegal() {
        IllegalArgumentException result = Assert.assertThrows(IllegalArgumentException.class, () -> BatchActionClassTagUtil.obtain("no-slash"));

        assertThat(result.getMessage(), is("illegal requestPath format. requestPath='no-slash'."));
    }
}