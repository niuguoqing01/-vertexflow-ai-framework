package com.vertexflow.ai.rag;

public class SimpleTextEmbedding {

    private final int dimension;

    public SimpleTextEmbedding(int dimension) {
        this.dimension = dimension;
    }

    public double[] embed(String text) {
        double[] vector = new double[dimension];

        if (text == null || text.isBlank()) {
            return vector;
        }

        String normalized = text.toLowerCase();
        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            int index = Math.abs(c) % dimension;
            vector[index] += 1.0;
        }

        normalize(vector);
        return vector;
    }

    private void normalize(double[] vector) {
        double sum = 0.0;
        for (double v : vector) {
            sum += v * v;
        }

        double norm = Math.sqrt(sum);
        if (norm == 0) {
            return;
        }

        for (int i = 0; i < vector.length; i++) {
            vector[i] = vector[i] / norm;
        }
    }
}
