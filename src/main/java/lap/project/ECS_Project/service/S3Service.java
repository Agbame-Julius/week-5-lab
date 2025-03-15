package lap.project.ECS_Project.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class S3Service {
    private String bucketName = "my-file-bucket-for-project";

    @Autowired
    private AmazonS3 amazonS3;

    public String generatePresignedUrl(String fileKey) {
        java.util.Date expiration = new java.util.Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 60; // 1 hour expiration
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, fileKey)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        return amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString();
    }



    // Method to upload files to S3
    public String uploadFiles(MultipartFile files) throws IOException {
            // Generate a unique file key
            String originalFilename = StringUtils.cleanPath(files.getOriginalFilename());
            String fileKey = UUID.randomUUID().toString() + "_" + originalFilename;

            // Determine file type
            String fileType = determineFileType(originalFilename);

            // Upload to S3
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(files.getContentType());
            metadata.setContentLength(files.getSize());

            amazonS3.putObject(new PutObjectRequest(
                    bucketName,
                    fileKey,
                    files.getInputStream(),
                    metadata
            ).withCannedAcl(CannedAccessControlList.Private));


        return fileKey;
    }

    /**
     * Retrieves a file from S3 bucket as an InputStream
     * @param fileKey The key (name) of the file to retrieve
     * @return InputStream of the file content
     * @throws IOException If an error occurs during file retrieval
     */
    public InputStream getFileFromS3(String fileKey) throws IOException {
        try {
            S3Object s3Object = amazonS3.getObject(bucketName, fileKey);
            return s3Object.getObjectContent();
        } catch (AmazonS3Exception e) {
            throw new IOException("Error retrieving file from S3: " + e.getMessage(), e);
        }
    }



    /**
     * Lists all files in the S3 bucket
     * @return List of file information (key and name)
     */
    public List<Map<String, String>> listFiles() {
        List<Map<String, String>> fileList = new ArrayList<>();

        ListObjectsV2Result result = amazonS3.listObjectsV2(bucketName);
        List<S3ObjectSummary> objects = result.getObjectSummaries();

        for (S3ObjectSummary os : objects) {
            String fileKey = os.getKey();
            String fileName = fileKey.substring(fileKey.indexOf('_') + 1);

            Map<String, String> fileMap = new HashMap<>();
            fileMap.put("key", fileKey);
            fileMap.put("name", fileName);
            fileMap.put("size", String.valueOf(os.getSize()));
            fileMap.put("lastModified", os.getLastModified().toString());

            fileList.add(fileMap);
        }

        return fileList;
    }

    // Helper method to determine file type
    private String determineFileType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

        switch (extension) {
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
                return "image";
            case "pdf":
                return "pdf";
            case "doc":
            case "docx":
                return "doc";
            case "xls":
            case "xlsx":
            case "csv":
                return "excel";
            case "mp4":
            case "avi":
            case "mov":
                return "video";
            default:
                return "other";
        }
    }

}
