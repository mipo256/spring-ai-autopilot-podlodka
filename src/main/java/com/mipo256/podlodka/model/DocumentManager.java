package com.mipo256.podlodka.model;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

@Service
public class DocumentManager {

    @Autowired
    private VectorStore vectorStore;

    public void loadToVectorStore(String content) {
        TextReader textReader = new TextReader(new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)));
        List<Document> documents = textReader.get();

        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter(1536, 1, 1, Integer.MAX_VALUE, true);
        List<Document> splitDocuments = tokenTextSplitter.split(documents);

        vectorStore.add(splitDocuments);
    }
}
