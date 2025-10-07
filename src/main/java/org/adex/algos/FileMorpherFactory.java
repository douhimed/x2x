package org.adex.algos;

import org.adex.models.Input;
import org.adex.models.Output;

import java.util.Map;

public final class FileMorpherFactory {

    private FileMorpherFactory() {
        throw new RuntimeException("Bruuh...!");
    }

    private static final Properties2Yml PROPERTIES_2_YML = new Properties2Yml();

    private static final Map<String, Map<String, FileMorpherAlgo>> ALGOS = Map.of(
            "properties", Map.of("yml", PROPERTIES_2_YML, "yaml", PROPERTIES_2_YML)
    );

    public static Output execute(Input in) {
        return ALGOS.get(in.extension()).get(in.dest()).getAlgo().apply(in);
    }
}
