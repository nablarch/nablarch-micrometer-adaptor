package nablarch.integration.micrometer.instrument.binder.jmx;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * {@link MBeanAttributeCondition}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class MBeanAttributeConditionTest {

    @Test
    public void testGetters() {
        MBeanAttributeCondition sut = new MBeanAttributeCondition("objectName", "attribute");

        assertThat(sut.getObjectName(), is("objectName"));
        assertThat(sut.getAttribute(), is("attribute"));
    }
}