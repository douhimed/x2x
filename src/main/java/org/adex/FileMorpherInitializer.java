package org.adex;

import org.adex.algos.FileMorpherFactory;
import org.adex.models.Input;
import org.adex.models.Output;
import org.adex.validators.DestinationValidator;
import org.adex.validators.SourceValidator;
import org.adex.validators.Validator;

import java.nio.file.Path;

public final class FileMorpherInitializer {

    private static final Validator<Path> SRC_VALIDATOR = new SourceValidator();
    private static final Validator<String> DEST_VALIDATOR = new DestinationValidator();

    private Path src;
    private String dest;

    private Output output;

    public FileMorpherInitializer from(String src) {
        this.src = Path.of(src);
        return this;
    }

    public FileMorpherInitializer to(String dest) {
        this.dest = dest;
        return this;
    }

    public FileMorpherInitializer start() {
        validate();
        output = FileMorpherFactory.execute(new Input(src, getExtension(), dest));
        return this;
    }

    public Path path() {
        return output.path();
    }

    public String getFileName() {
        return output.path().toAbsolutePath().toString();
    }

    private void validate() {
        SRC_VALIDATOR.validate(src);
        DEST_VALIDATOR.validate(dest);
    }

    private String getExtension() {
        final String fileName = src.getFileName().toString();
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
