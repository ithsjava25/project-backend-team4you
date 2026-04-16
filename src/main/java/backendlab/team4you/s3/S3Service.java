package backendlab.team4you.s3;

import backendlab.team4you.exceptions.FileKeyConflictException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import software.amazon.awssdk.services.s3.model.S3Exception;


import java.io.InputStream;

// Tells Spring to manage this class as a service bean
@Service
@ConditionalOnProperty(name = "aws.access-key")
public class S3Service {
    private final S3Client s3Client;

    @Value("${aws.bucket-name}")
    private String bucketName;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    // Upload a file to S3
    public void uploadFile(String key, byte[] data, String contentType) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(data)
        );
    }

    // Download a file from S3
    public InputStream downloadFile(String key) {
        return s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build()
        );
    }

    // Delete a file from S3
    public void deleteFile(String key) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build()
        );
    }

    public void uploadFileIfAbsent(String key, byte[] data, String contentType) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(contentType)
                            .ifNoneMatch("*")
                            .build(),
                    RequestBody.fromBytes(data)
            );
        } catch (S3Exception exception) {
            if (exception.statusCode() == 409 || exception.statusCode() == 412) {
                throw new FileKeyConflictException(key, exception);
            }
            throw exception;
        }
    }
}
