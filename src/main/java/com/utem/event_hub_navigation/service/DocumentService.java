package com.utem.event_hub_navigation.service;

import org.springframework.web.multipart.MultipartFile;

import com.utem.event_hub_navigation.model.Document;

public interface DocumentService {

    Document storeFile(MultipartFile file);

    Document getFile(Integer fileId);

}