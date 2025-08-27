package com.utem.event_hub_navigation.utils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.utem.event_hub_navigation.dto.QRPayload;

@Component
public class QRCodeUtil {

    private static final String HMAC_ALGO = "HmacSHA256";

    private final byte[] secretKeyBytes;
    private final ObjectMapper mapper = new ObjectMapper();

    public QRCodeUtil(@Value("${attendance.qr.secretKey}") String secretKey) {
        this.secretKeyBytes = Base64.getUrlDecoder().decode(secretKey);
    }

    public String createPayload(int eventId,
            int sessionId,
            LocalDateTime expiresAt) throws Exception {
        // 1) Format expiresAt to ISO string
        String expires = expiresAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // 2) Build the string to sign
        String dataToSign = eventId + "|" + sessionId + "|" + expires;

        // 3) Compute HMAC_SHA256
        Mac mac = Mac.getInstance(HMAC_ALGO);
        mac.init(new SecretKeySpec(secretKeyBytes, HMAC_ALGO));
        String sig = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mac.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8)));

        // 4) Build payload object
        QRPayload p = new QRPayload("attendance",eventId, sessionId, expires, sig);

        // 5. Serialize to JSON and Base64 encode
        // String json = mapper.writeValueAsString(p);
        // String encoded = Base64.getUrlEncoder().withoutPadding()
        //         .encodeToString(json.getBytes(StandardCharsets.UTF_8));

        // // 6. Return full URL
        // return "http://192.168.3.109:3000/public/check-in?q=" + encoded;
        // 5. Serialize to JSON string
        return mapper.writeValueAsString(p);
    }

    public byte[] generateQRCodeImage(String payload, int width, int height) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(payload, BarcodeFormat.QR_CODE, width, height);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", out);
            return out.toByteArray();
        }
    }

    // public QRPayload validateQRCode(String encodedPayload) throws Exception {
    //     // Decode and parse QR payload
    //     byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedPayload);
    //     String json = new String(decodedBytes, StandardCharsets.UTF_8);
    //     QRPayload payload = new ObjectMapper().readValue(json, QRPayload.class);

    //     // Validate signature
    //     String toSign = payload.eventId + "|" + payload.sessionId + "|" + payload.expiresAt;
    //     Mac mac = Mac.getInstance("HmacSHA256");
    //     mac.init(new SecretKeySpec(secretKeyBytes, "HmacSHA256"));
    //     String expectedSig = Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(toSign.getBytes()));
    //     if (!expectedSig.equals(payload.sig)) {
    //         throw new IllegalArgumentException("Invalid QR signature.");
    //     }

    //     // Validate expiration
    //     LocalDateTime expires = LocalDateTime.parse(payload.expiresAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    //     if (expires.isBefore(LocalDateTime.now())) {
    //         throw new IllegalArgumentException("QR code expired.");
    //     }

    //     // Example: Save check-in record to DB
    //     // attendanceRepository.save(new AttendanceRecord(participantId,
    //     // payload.sessionId, LocalDateTime.now()));

    //     return payload;
    // }


    public QRPayload validateJSONQRCode(String jsonPayload) throws Exception {
        // 1. Parse the JSON payload directly (assuming the QR code holds the JSON string)
        QRPayload payload;
        try {
            payload = new ObjectMapper().readValue(jsonPayload, QRPayload.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid QR code format.", e);
        }
    
        // 2. Validate the signature
        String dataToSign = payload.eventId + "|" + payload.sessionId + "|" + payload.expiresAt;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secretKeyBytes, "HmacSHA256"));
        String expectedSignature = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mac.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8)));
    
        if (!expectedSignature.equals(payload.sig)) {
            throw new SecurityException("Invalid QR code signature.");
        }
    
        // 3. Validate expiration
        LocalDateTime expiresAt = LocalDateTime.parse(payload.expiresAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        if (expiresAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("QR code has expired.");
        }
    
        // 4. Return the validated payload for further processing
        return payload;
    }
}
