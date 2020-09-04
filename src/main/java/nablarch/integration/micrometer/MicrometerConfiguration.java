package nablarch.integration.micrometer;

import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.externalize.CompositeExternalizedLoader;
import nablarch.core.repository.di.config.externalize.ExternalizedComponentDefinitionLoader;
import nablarch.core.repository.di.config.externalize.OsEnvironmentVariableExternalizedLoader;
import nablarch.core.repository.di.config.externalize.SystemPropertyExternalizedLoader;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;

import java.util.Arrays;
import java.util.List;

/**
 * Micrometerの設定を読み込むためのクラス。
 * <p>
 * このクラスは{@link DiContainer}の仕組みを流用して、設定ファイルの情報をロードする。<br>
 * </p>
 * <p>
 * デフォルトではクラスパス配下の{@code nablarch/integration/micrometer/micrometer.xml}を読み込む。<br>
 * このファイルはデフォルトコンフィギュレーションのモジュールに含まれており、
 * クラスパスルートの{@code micrometer.properties}を読み込むように定義されている。<br>
 * したがって、利用者はアプリケーションのクラスパスルートに{@code micrometer.properties}を
 * 配置することでMicrometerの設定を指定できる。
 * </p>
 * <p>
 * また、{@link ExternalizedComponentDefinitionLoader}として{@link OsEnvironmentVariableExternalizedLoader}と
 * {@link SystemPropertyExternalizedLoader}を使用している。<br>
 * これにより、OS環境変数またはシステムプロパティで設定値を上書きすることができる。
 * </p>
 *
 * @author Tanaka Tomoyuki
 */
public class MicrometerConfiguration extends DiContainer {
    private static final String DEFAULT_CONFIG_PATH = "nablarch/integration/micrometer/micrometer.xml";

    /**
     * デフォルトコンフィギュレーションに含まれる設定ファイルを読み込んでインスタンスを生成する。
     */
    public MicrometerConfiguration() {
        this(DEFAULT_CONFIG_PATH);
    }

    /**
     * 読み込む設定ファイルのパスを指定してインスタンスを生成する。
     * @param xmlFilePath 読み込む設定ファイルのパス
     */
    public MicrometerConfiguration(String xmlFilePath) {
        super(new XmlComponentDefinitionLoader(xmlFilePath));
    }

    @Override
    protected ExternalizedComponentDefinitionLoader loadExternalizedComponentDefinitionLoader() {
        List<ExternalizedComponentDefinitionLoader> loaders = Arrays.asList(
            new OsEnvironmentVariableExternalizedLoader(),
            new SystemPropertyExternalizedLoader()
        );

        return new CompositeExternalizedLoader(loaders);
    }
}
