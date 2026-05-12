package com.travel.plan.service;

import java.io.InputStream;

public interface FileService {

    void upload(String key, InputStream inputStream, String contentType, long size);

    InputStream download(String key);

    void delete(String key);

    void deleteByPrefix(String prefix);
}
