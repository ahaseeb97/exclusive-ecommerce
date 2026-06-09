/*
 * Common S3 Service for generating presigned URLs
 */
package com.microservice.Helper.s3.service;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Centralized S3 Service for generating presigned URLs
 * This service can be used across all microservices
 * 
 * @author abdul.haseeb
 */
@Service
public class S3Service {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    @Value("${aws.s3.presigned-url-expiration-minutes:60}")
    private int expirationMinutes;
    
    @Value("${s3.image.path.prefix:}")
    private String s3ImagePath;
    
    Region clientRegion = Region.AP_NORTHEAST_1;

    /**
     * Generates a presigned URL for an S3 object
     * 
     * @param objectKey The S3 object key/path (e.g., "laptop.jpg" or "products/laptop.jpg")
     * @return Presigned URL string, or null if objectKey is null or empty
     */
    public String generatePresignedUrl(String objectKey) {
        if (objectKey == null || objectKey.trim().isEmpty()) {
            return null;
        }

        // Create AWS credentials from access key and secret key
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        try (S3Presigner presigner = S3Presigner.builder()
                .region(clientRegion)
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build()) {

            // Build the full S3 key with prefix if provided
            String fullKey = (s3ImagePath != null && !s3ImagePath.trim().isEmpty()) 
                    ? s3ImagePath + objectKey 
                    : objectKey;

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fullKey)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(
                    presignerRequest -> presignerRequest
                            .signatureDuration(Duration.ofMinutes(expirationMinutes))
                            .getObjectRequest(getObjectRequest)
            );

            return presignedRequest.url().toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL for object: " + objectKey, e);
        }
    }
    
    /**
     * Uploads a file to S3 bucket
     * 
     * @param inputStream The input stream of the file to upload
     * @param originalFileName The original filename (used to determine extension)
     * @param contentType The content type of the file (e.g., "image/jpeg", "image/png")
     * @return The S3 object key/path where the file was uploaded
     */
    public String uploadFile(InputStream inputStream, String originalFileName, String contentType) {
        if (inputStream == null || originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new IllegalArgumentException("Input stream and filename cannot be null or empty");
        }

        try {
            // Create AWS credentials
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
            
            // Generate unique filename to avoid conflicts
            String fileExtension = "";
            int lastDotIndex = originalFileName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                fileExtension = originalFileName.substring(lastDotIndex);
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            
            // Build the full S3 key with prefix if provided
            String objectKey = (s3ImagePath != null && !s3ImagePath.trim().isEmpty()) 
                    ? s3ImagePath + uniqueFileName 
                    : uniqueFileName;
            
            // Create S3 client
            try (S3Client s3Client = S3Client.builder()
                    .region(clientRegion)
                    .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                    .build()) {
                
                // Read all bytes from input stream
                byte[] fileBytes = inputStream.readAllBytes();
                
                // Create put object request
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .contentType(contentType != null ? contentType : "application/octet-stream")
                        .build();
                
                // Upload the file
                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));
                
                return objectKey;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to S3: " + originalFileName, e);
        }
    }
    
    /**
     * Uploads a file to S3 bucket with a custom object key
     * 
     * @param inputStream The input stream of the file to upload
     * @param objectKey The desired S3 object key/path
     * @param contentType The content type of the file
     * @return The S3 object key/path where the file was uploaded
     */
    public String uploadFileWithKey(InputStream inputStream, String objectKey, String contentType) {
        if (inputStream == null || objectKey == null || objectKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Input stream and object key cannot be null or empty");
        }

        try {
            // Create AWS credentials
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
            
            // Build the full S3 key with prefix if provided
            String fullKey = (s3ImagePath != null && !s3ImagePath.trim().isEmpty()) 
                    ? s3ImagePath + objectKey 
                    : objectKey;
            
            // Create S3 client
            try (S3Client s3Client = S3Client.builder()
                    .region(clientRegion)
                    .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                    .build()) {
                
                // Read all bytes from input stream
                byte[] fileBytes = inputStream.readAllBytes();
                
                // Create put object request
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fullKey)
                        .contentType(contentType != null ? contentType : "application/octet-stream")
                        .build();
                
                // Upload the file
                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));
                
                return fullKey;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to S3: " + objectKey, e);
        }
    }
    
    /**
     * Deletes a file from S3 bucket
     * 
     * @param objectKey The S3 object key/path to delete (e.g., "abc123.jpg" or "products/abc123.jpg")
     * @throws RuntimeException if deletion fails
     */
    public void deleteFile(String objectKey) {
        if (objectKey == null || objectKey.trim().isEmpty()) {
            return; // Nothing to delete
        }

        try {
            // Create AWS credentials
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
            
            // Build the full S3 key with prefix if provided
            String fullKey = (s3ImagePath != null && !s3ImagePath.trim().isEmpty()) 
                    ? s3ImagePath + objectKey 
                    : objectKey;
            
            // Create S3 client
            try (S3Client s3Client = S3Client.builder()
                    .region(clientRegion)
                    .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                    .build()) {
                
                // Create delete object request
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fullKey)
                        .build();
                
                // Delete the file
                s3Client.deleteObject(deleteObjectRequest);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from S3: " + objectKey, e);
        }
    }
    
    /**
     * Deletes multiple files from S3 bucket
     * 
     * @param objectKeys List of S3 object keys/paths to delete
     * @return List of successfully deleted keys
     */
    public List<String> deleteFiles(List<String> objectKeys) {
        if (objectKeys == null || objectKeys.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> deletedKeys = new ArrayList<>();
        for (String objectKey : objectKeys) {
            try {
                deleteFile(objectKey);
                deletedKeys.add(objectKey);
            } catch (Exception e) {
                // Log error but continue with other deletions
                System.err.println("Failed to delete file: " + objectKey + " - " + e.getMessage());
            }
        }
        return deletedKeys;
    }
}

