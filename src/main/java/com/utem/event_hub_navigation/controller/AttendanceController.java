// package com.utem.event_hub_navigation.controller;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ContentDisposition;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;

// import com.utem.event_hub_navigation.dto.AttendanceRequest;
// // import com.utem.event_hub_navigation.service.AttendanceService;
// import com.utem.event_hub_navigation.utils.QRCodeGenerator;

// @RestController
// @RequestMapping("/attendance")
// public class AttendanceController {

//     @Autowired
//     private AttendanceService attendanceService;

//     // @PostMapping("/mark")
//     // public ResponseEntity<String> markAttendance(@RequestBody AttendanceRequest request) {
//     //     String response = attendanceService.markAttendance(request.getUserId(), request.getQrData());
//     //     return ResponseEntity.ok(response);
//     // }

//     @GetMapping(value = "/qr_code_download", produces = MediaType.IMAGE_PNG_VALUE)
//     public ResponseEntity<byte[]> downloadQRCode(@RequestParam String text) {
//         try {
//             byte[] image = QRCodeGenerator.generateQRCodeImage(text, 300, 300);

//             HttpHeaders headers = new HttpHeaders();
//             headers.setContentType(MediaType.IMAGE_PNG);
//             headers.setContentDisposition(ContentDisposition.builder("attachment")
//                 .filename("qrcode.png")
//                 .build());

//             return new ResponseEntity<>(image, headers, HttpStatus.OK);
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//         }
//     }
// }
