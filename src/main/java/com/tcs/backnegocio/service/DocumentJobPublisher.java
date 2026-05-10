package com.tcs.backnegocio.service;

public interface DocumentJobPublisher {

    void enqueueDocument(Integer documentId, String filePath);
}
