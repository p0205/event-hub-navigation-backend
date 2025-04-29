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

@Component
public class QRCodeGenerator {

    private static final String HMAC_ALGO = "HmacSHA256";

    private final byte[] secretKeyBytes;
    private final ObjectMapper mapper = new ObjectMapper();

    public QRCodeGenerator(@Value("${attendance.qr.secretKey}") String secretKey) {
        this.secretKeyBytes = Base64.getUrlDecoder().decode(secretKey);
    }

    public String createPayload(int eventId,
                                int eventVenueId,
                                LocalDateTime expiresAt) throws Exception {
        // 1) Format expiresAt to ISO string
        String expires = expiresAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // 2) Build the string to sign
        String dataToSign = eventId + "|" + eventVenueId + "|" + expires;

        // 3) Compute HMAC_SHA256
        Mac mac = Mac.getInstance(HMAC_ALGO);
        mac.init(new SecretKeySpec(secretKeyBytes, HMAC_ALGO));
        String sig = Base64.getUrlEncoder().withoutPadding()
                           .encodeToString(mac.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8)));

        // 4) Build payload object
        QRPayload p = new QRPayload(eventId, eventVenueId, expires, sig);

        // 5) Serialize to JSON and Base64-encode
        String json = mapper.writeValueAsString(p);
        return Base64.getUrlEncoder().withoutPadding()
                     .encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    public byte[] generateQRCodeImage(String payload, int width, int height) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(payload, BarcodeFormat.QR_CODE, width, height);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", out);
            return out.toByteArray();
        }
    }

    // Simple DTO
    public static class QRPayload {
        public int eventId;
        public int eventVenueId;
        public String expiresAt;
        public String sig;
        public QRPayload(int eventId, int eventVenueId, String expiresAt, String sig) {
            this.eventId = eventId;
            this.eventVenueId = eventVenueId;
            this.expiresAt = expiresAt;
            this.sig = sig;
        }
    }
}
