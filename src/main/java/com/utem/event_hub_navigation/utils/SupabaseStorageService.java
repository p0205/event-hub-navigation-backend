package com.utem.event_hub_navigation.utils;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-key}")
    private String serviceKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String uploadFile(MultipartFile file, String bucketName, String path) throws IOException {
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + path;

        HttpHeaders headers = new HttpHeaders();
       
        headers.set("apikey", serviceKey); // service role key
        headers.set("Authorization", "Bearer " + serviceKey); // also service role key
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM); // Use octet-stream for raw bytes
        

        HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);

        ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl,
                HttpMethod.PUT,
                entity,
                String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + path;
        } else {
            throw new RuntimeException("Upload failed: " + response.getBody());
        }
    }

    public byte[] downloadFile(String bucketName, String path) {
        String downloadUrl = supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + path;

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", serviceKey);
        headers.set("Authorization", "Bearer " + serviceKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                downloadUrl,
                HttpMethod.GET,
                entity,
                byte[].class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("Download failed: " + response.getBody());
        }
    }
}
