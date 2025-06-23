package com.utem.event_hub_navigation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.utem.event_hub_navigation.dto.DocumentResponse;
import com.utem.event_hub_navigation.model.Document;
import com.utem.event_hub_navigation.service.DocumentService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PostMapping("/api/uploadFile")
    public DocumentResponse uploadFile(@RequestParam("file") MultipartFile file) {
        Document fileName = documentService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/downloadFile/")
            .path(fileName.getFilename())
            .toUriString();

            System.out.println(fileName.getFilename() + " " + file.getSize());
        DocumentResponse response = DocumentResponse.builder()
            .fileName(fileName.getFilename())
            .fileDownloadUri(fileDownloadUri)
            .fileType(fileName.getFileType())
            .size(file.getSize())
            .build();
        // Save the file information to the database
        return response;
    }

         @GetMapping("/downloadFile/{fileId}")
    public ResponseEntity <Resource> downloadFile(@PathVariable Integer fileId, HttpServletRequest request) {
        // Load file as Resource
        Document databaseFile = documentService.getFile(fileId);

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(databaseFile.getFileType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + databaseFile.getFilename() + "\"")
            .body(new ByteArrayResource(databaseFile.getData()));
    }
}
