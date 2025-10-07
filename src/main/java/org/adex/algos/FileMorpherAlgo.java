package org.adex.algos;

import org.adex.models.Input;
import org.adex.models.Output;

import java.util.function.Function;

public interface FileMorpherAlgo {
    Function<Input, Output> getAlgo();
}
