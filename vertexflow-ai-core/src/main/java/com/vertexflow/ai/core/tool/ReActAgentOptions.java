package com.vertexflow.ai.core.tool;

public class ReActAgentOptions {

    private int maxSteps = 5;
    private boolean returnSteps = true;
    private boolean allowJsonActionInput = true;
    private boolean retryOnFormatError = true;

    public boolean isRetryOnFormatError() {
        return retryOnFormatError;
    }

    private ReActAgentOptions() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ReActAgentOptions defaults() {
        return builder().build();
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public boolean isReturnSteps() {
        return returnSteps;
    }

    public boolean isAllowJsonActionInput() {
        return allowJsonActionInput;
    }

    public static class Builder {

        private final ReActAgentOptions options = new ReActAgentOptions();

        public Builder maxSteps(int maxSteps) {
            if (maxSteps <= 0) {
                throw new IllegalArgumentException("maxSteps must be greater than 0");
            }

            options.maxSteps = maxSteps;
            return this;
        }

        public Builder returnSteps(boolean returnSteps) {
            options.returnSteps = returnSteps;
            return this;
        }

        public Builder allowJsonActionInput(boolean allowJsonActionInput) {
            options.allowJsonActionInput = allowJsonActionInput;
            return this;
        }

        public ReActAgentOptions build() {
            return options;
        }

        public Builder retryOnFormatError(boolean retryOnFormatError) {
            options.retryOnFormatError = retryOnFormatError;
            return this;
        }
    }
}