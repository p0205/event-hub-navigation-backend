package com.utem.event_hub_navigation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class DocumentResponse {
    private String fileName;
    private String fileDownloadUri;
    private String fileType;
    private long size;

}
