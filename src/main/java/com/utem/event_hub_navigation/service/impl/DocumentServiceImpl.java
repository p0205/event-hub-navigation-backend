package com.utem.event_hub_navigation.service.impl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.utem.event_hub_navigation.model.Document;
import com.utem.event_hub_navigation.repo.DocumentRepo;
import com.utem.event_hub_navigation.service.DocumentService;

@Service
public class DocumentServiceImpl implements DocumentService {

    @Autowired
    private DocumentRepo documentRepo;

    @Override
    public Document storeFile(MultipartFile file) {
        // Normalize file name
        String originalFilename = file.getOriginalFilename();
        String fileName = (originalFilename != null) ? StringUtils.cleanPath(originalFilename) : "defaultFileName";

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                System.out.println("Sorry! Filename contains invalid path sequence " + fileName);
                // throw new Exception("Sorry! Filename contains invalid path sequence " +
                // fileName);
            }

            Document dbFile = Document.builder()
                    .filename(fileName)
                    .fileType(file.getContentType())
                    .data(file.getBytes())
                    .build();

            return documentRepo.save(dbFile);
        } catch (IOException ex) {
            System.out.println("Could not store file " + fileName + ". Please try again!");
            return null;
            // throw new Exception("Could not store file " + fileName + ". Please try
            // again!", ex);
        }
    }

    @Override
    public Document getFile(Integer fileId) {
        return documentRepo.findById(fileId).orElse(null);

    }

}
