package javapolis.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.InputStream;

@Controller
@RequestMapping("/images")
public class ImageController {

    @GetMapping("/{imageName:.+}")
    public ResponseEntity<byte[]> getImage(@PathVariable String imageName) {
        try {
            ClassPathResource resource = new ClassPathResource("images/" + imageName);
            
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            try (InputStream inputStream = resource.getInputStream()) {
                byte[] imageBytes = inputStream.readAllBytes();
                
                // Определяем MIME тип на основе расширения файла
                String contentType = determineContentType(imageName);
                
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageBytes);
            }
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private String determineContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }
}
