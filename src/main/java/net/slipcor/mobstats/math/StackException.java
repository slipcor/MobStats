package net.slipcor.mobstats.math;

public class StackException extends IllegalArgumentException {
    StackException(String input, String error) {
        super(
                String.format(
                        "Failed to parse input: %s\n%s",
                        input,
                        error
                )
        );
    }
}
