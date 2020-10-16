package nablarch.integration.micrometer.instrument.handler.http;

import java.lang.reflect.Method;
import java.util.List;

/**
 * {@link DefaultHttpRequestMetricsTagBuilder}の {@code class} と {@code method} タグの
 * フォーマットを検証するためのテスト用コントローラクラス。
 *
 * @author Tanaka Tomoyuki
 */
public class TestController {
    /**
     * 引数なしのメソッド。
     */
    static final Method ACTION_METHOD_WITHOUT_ARGS;

    /**
     * 引数が複数存在するメソッド。
     */
    static final Method ACTION_METHOD_WITH_ARGS;

    /**
     * 引数が配列のメソッド。
     */
    static final Method ACTION_METHOD_ARRAY;

    /**
     * 引数が入れ子の（多次元）配列のメソッド。
     */
    static final Method ACTION_METHOD_NESTED_ARRAY;

    /**
     * 引数がメンバークラスのメソッド。
     */
    static final Method ACTION_METHOD_MEMBER_CLASS;

    /**
     * 引数が入れ子のメンバークラスのメソッド。
     */
    static final Method ACTION_METHOD_NESTED_MEMBER_CLASS;

    /**
     * 引数に総称型の型が使われているメソッド。
     */
    static final Method ACTION_METHOD_GENERIC;

    static {
        try {
            ACTION_METHOD_WITHOUT_ARGS = TestController.class.getMethod("withoutArgs");
            ACTION_METHOD_WITH_ARGS = TestController.class.getMethod("withArgs", int.class, String.class);
            ACTION_METHOD_ARRAY = TestController.class.getMethod("array", String[].class);
            ACTION_METHOD_NESTED_ARRAY = TestController.class.getMethod("nestedArray", String[][].class);
            ACTION_METHOD_MEMBER_CLASS = TestController.class.getMethod("memberClass", MemberClass.class);
            ACTION_METHOD_NESTED_MEMBER_CLASS = TestController.class.getMethod("nestedMemberClass", MemberClass.NestedMemberClass.class);
            ACTION_METHOD_GENERIC = TestController.class.getMethod("generic", List.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void withoutArgs() {}

    public void withArgs(int number, String text) {}

    public void array(String[] array) {}

    public void nestedArray(String[][] nestedArray) {}

    public void memberClass(MemberClass memberClass) {}

    public void nestedMemberClass(MemberClass.NestedMemberClass nestedMemberClass) {}

    public void generic(List<String> generic) {}

    /**
     * メソッド引数がメンバークラスのケースを検証するためのクラス。
     */
    public static class MemberClass {
        public static class NestedMemberClass {}
    }

    /**
     * コントローラがメンバークラスのケースを検証するためのコントローラクラス。
     */
    public static class MemberController {
        public static final Method ACTION_METHOD;

        static {
            try {
                ACTION_METHOD = MemberController.class.getMethod("method");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void method() {}
    }
}
