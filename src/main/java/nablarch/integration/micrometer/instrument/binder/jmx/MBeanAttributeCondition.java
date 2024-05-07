package nablarch.integration.micrometer.instrument.binder.jmx;

import nablarch.core.util.annotation.Published;

/**
 * JMXで取得するMBeanのAttributeを特定するための、オブジェクト名と属性名を保持したデータクラス。
 * @author Tanaka Tomoyuki
 */
@Published(tag = "architect")
public class MBeanAttributeCondition {
    /** オブジェクト名。 */
    private final String objectName;
    /** 属性名。 */
    private final String attribute;

    /**
     * オブジェクト名と属性を指定するコンストラクタ。
     * @param objectName オブジェクト名
     * @param attribute 属性名
     */
    public MBeanAttributeCondition(String objectName, String attribute) {
        this.objectName = objectName;
        this.attribute = attribute;
    }

    /**
     * オブジェクト名を取得する。
     * @return オブジェクト名
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * 属性名を取得する。
     * @return 属性名
     */
    public String getAttribute() {
        return attribute;
    }
}
