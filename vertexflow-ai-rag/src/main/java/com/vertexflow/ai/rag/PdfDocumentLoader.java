package com.vertexflow.ai.rag;

import com.vertexflow.ai.core.exception.AiErrorCode;
import com.vertexflow.ai.core.exception.AiException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.nio.file.Path;

public class PdfDocumentLoader {

    private PdfDocumentLoader() {
    }

    public static Document loadFile(String filePath) {
        return load(Path.of(filePath));
    }

    public static Document load(Path path) {
        if (path == null) {
            throw new AiException(AiErrorCode.INVALID_REQUEST, "PDF path must not be null");
        }

        File file = path.toFile();

        if (!file.exists()) {
            throw new AiException(AiErrorCode.INVALID_REQUEST, "PDF file does not exist: " + path);
        }

        if (!file.isFile()) {
            throw new AiException(AiErrorCode.INVALID_REQUEST, "PDF path is not a file: " + path);
        }

        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            throw new AiException(AiErrorCode.INVALID_REQUEST, "File is not a PDF: " + path);
        }

        try (PDDocument pdf = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(pdf);

            if (text == null || text.isBlank()) {
                throw new AiException(AiErrorCode.INVALID_REQUEST, "PDF text is empty: " + path);
            }

            return new Document(file.getName(), text.trim());
        } catch (AiException e) {
            throw e;
        } catch (Exception e) {
            throw new AiException(AiErrorCode.RAG_ERROR, "Failed to load PDF file: " + path, e);
        }
    }
}