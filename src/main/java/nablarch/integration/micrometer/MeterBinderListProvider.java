package nablarch.integration.micrometer;

import io.micrometer.core.instrument.binder.MeterBinder;

import java.util.List;

/**
 * {@link MeterBinder}のリストを提供するインターフェース。
 *
 * @author Tanaka Tomoyuki
 */
public interface MeterBinderListProvider {

    /**
     * {@link MeterBinder}のリストを提供する。
     * @return {@link MeterBinder}のリスト
     */
    List<MeterBinder> provide();
}
