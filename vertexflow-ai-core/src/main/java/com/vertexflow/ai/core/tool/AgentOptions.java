package com.vertexflow.ai.core.tool;

public class AgentOptions {

    private int maxSteps = 10;
    private boolean returnSteps = true;

    private AgentOptions() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static AgentOptions defaults() {
        return builder().build();
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public boolean isReturnSteps() {
        return returnSteps;
    }

    public static class Builder {

        private final AgentOptions options = new AgentOptions();

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

        public AgentOptions build() {
            return options;
        }
    }
}