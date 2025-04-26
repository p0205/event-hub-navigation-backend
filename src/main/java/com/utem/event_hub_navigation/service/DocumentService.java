package com.utem.event_hub_navigation.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.utem.event_hub_navigation.model.Document;
import com.utem.event_hub_navigation.repo.DocumentRepo;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepo documentRepo;


     public Document storeFile(MultipartFile file) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                System.out.println("Sorry! Filename contains invalid path sequence " + fileName);
                // throw new Exception("Sorry! Filename contains invalid path sequence " + fileName);
            }

            Document dbFile =  Document.builder()
                    .filename(fileName)
                    .fileType(file.getContentType())
                    .data(file.getBytes())
                    .build();


            return documentRepo.save(dbFile);
        } catch (IOException ex) {
            System.out.println("Could not store file " + fileName + ". Please try again!");
            return null;
            // throw new Exception("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Document getFile(Integer fileId) {
        return documentRepo.findById(fileId).orElse(null);
             
    }

    


}
