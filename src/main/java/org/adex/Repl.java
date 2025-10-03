package org.adex;

public interface Repl {

    static void main(String[] args) {

        if (args.length < 2) {
            throw new IllegalArgumentException("Please specify a source file and a destination extension: java MyApp [source] [destination]");
        }

        IO.println(
                new FileMorpherInitializer()
                        .from(args[0])
                        .to(args[1])
                        .start()
        );
    }

}
