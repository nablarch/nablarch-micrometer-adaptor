package nablarch.integration.micrometer.otlp;

import io.micrometer.core.instrument.config.MeterRegistryConfigValidator;
import io.micrometer.core.instrument.config.validate.InvalidReason;
import io.micrometer.core.instrument.config.validate.PropertyValidator;
import io.micrometer.core.instrument.config.validate.Validated;
import io.micrometer.registry.otlp.OtlpConfig;
import nablarch.core.repository.di.DiContainer;
import nablarch.integration.micrometer.NablarchMeterRegistryConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link NablarchMeterRegistryConfig}を用いて{@link OtlpConfig}を実装したクラス。
 * @author Junya Koyama
 */
public class NablarchOtlpConfig extends NablarchMeterRegistryConfig implements OtlpConfig {
    /**
     * プレフィックスと{@link DiContainer}を指定してインスタンスを生成する。
     *
     * @param prefix      プレフィックス
     * @param diContainer {@link DiContainer}
     */
    public NablarchOtlpConfig(String prefix, DiContainer diContainer) {
        super(prefix, diContainer);
    }

    @Override
    protected String subPrefix() {
        return "otlp";
    }

    Map<String, String> headers() {
        if (headersString().trim().isEmpty()){
            return new HashMap<>();
        }

        return Stream.of(headersString().trim().split(",", -1))
                .map(keyValue -> keyValue.trim().split("=", -1))
                .collect(Collectors.toMap(keyValue -> keyValue[0].trim(), keyValue -> keyValue[1].trim(), (l, r) -> r));
    }

    private String headersString(){
        return PropertyValidator.getString(this, "headers").orElse("");
    }

    private static boolean isInvalidHeader(String keyValues){
        if (keyValues.trim().isEmpty()){
            return false;
        }
        return Stream.of(keyValues.trim().split(",", -1))
                .anyMatch(keyValue -> keyValue.trim().split("=", -1).length != 2);
    }

    @Override
    public Validated<?> validate() {
        return MeterRegistryConfigValidator.checkAll(this, (c) -> OtlpConfig.super.validate(),
                MeterRegistryConfigValidator.check("headers", NablarchOtlpConfig::headersString)
                        .andThen(i -> i.invalidateWhen(NablarchOtlpConfig::isInvalidHeader,
                                "headers: Invalid key-value", InvalidReason.MALFORMED)));
    }
}
