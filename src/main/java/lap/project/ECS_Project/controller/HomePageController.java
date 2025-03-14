package lap.project.ECS_Project.controller;

import lap.project.ECS_Project.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomePageController {
    @Autowired
    private S3Service s3Service;

    @GetMapping("/home")
    public String home() {
        return "index";
    }

    @GetMapping("/s3files")
    public String s3(){
        return "s3";
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("files") MultipartFile files) {
        Map<String, Object> response = new HashMap<>();

        try {
            String fileNames = s3Service.uploadFiles(files);

            if (fileNames.isEmpty()) {
                response.put("error", "Please select at least one file to upload");
                return ResponseEntity.badRequest().body(response);
            } else {
                response.put("success", true);
                response.put("message", fileNames + " file(s) uploaded successfully");
                response.put("files", fileNames);
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            response.put("error", "Error uploading files: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/files")
        public ResponseEntity<List<Map<String, String>>> listFiles() {
            List<Map<String, String>> files = s3Service.listFiles();
            files.forEach(file -> {
            String fileKey = file.get("key");
            String fileUrl = s3Service.generatePresignedUrl(fileKey);
            file.put("url", fileUrl); // Add the URL to the response
        });
        return ResponseEntity.ok(files);
        }


}
