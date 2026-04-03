package com.inboxintelligence.processor.persistence.storage;

public interface EmailStorageProvider {

    String readContent(String storagePath);

    String writeContent(String directoryPath, String fileName, String content);

}
