package com.vertexflow.ai.rag;

public class RagOptions {

    private int topK = 3;
    private boolean returnSources = true;

    public int getTopK() {
        return topK;
    }

    public RagOptions setTopK(int topK) {
        if (topK <= 0) {
            throw new IllegalArgumentException("topK must be greater than 0");
        }
        this.topK = topK;
        return this;
    }

    public boolean isReturnSources() {
        return returnSources;
    }

    public RagOptions setReturnSources(boolean returnSources) {
        this.returnSources = returnSources;
        return this;
    }

    public static RagOptions defaults() {
        return new RagOptions();
    }
}
