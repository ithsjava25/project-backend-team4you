package backendlab.team4you.s3;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

@Component
@ConditionalOnProperty(
        name = "aws.create-bucket-if-missing",
        havingValue = "true"
)
public class S3BucketInitializer {

    private static final Logger log = LoggerFactory.getLogger(S3BucketInitializer.class);

    private final S3Client s3Client;

    @Value("${aws.bucket-name}")
    private String bucketName;

    public S3BucketInitializer(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @PostConstruct
    public void ensureBucketExists() {
        try {
            s3Client.headBucket(builder -> builder.bucket(bucketName));
            log.info("S3 bucket already exists: {}", bucketName);
        } catch (NoSuchBucketException exception) {
            log.info("Creating local S3 bucket: {}", bucketName);
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
        }
    }
}
