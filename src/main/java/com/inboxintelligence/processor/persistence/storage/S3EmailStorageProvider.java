package com.inboxintelligence.processor.persistence.storage;

import org.springframework.stereotype.Component;

@Component
public class S3EmailStorageProvider implements EmailStorageProvider {

    @Override
    public String readContent(String storagePath) {
        throw new UnsupportedOperationException("S3EmailStorageProvider is not implemented");
    }

    @Override
    public String writeContent(String directoryPath, String fileName, String content) {
        throw new UnsupportedOperationException("S3EmailStorageProvider is not implemented");
    }

}
