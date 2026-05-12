package com.travel.plan.controller;

import com.travel.plan.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.net.URLConnection;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping("/api/files/**")
    public ResponseEntity<InputStreamResource> serveFile(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String key = requestUri.substring("/api/files/".length());

        if (key.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            InputStream stream = fileService.download(key);
            String contentType = URLConnection.guessContentTypeFromName(key);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setCacheControl(CacheControl.maxAge(1, java.util.concurrent.TimeUnit.HOURS).getHeaderValue());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(stream));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
